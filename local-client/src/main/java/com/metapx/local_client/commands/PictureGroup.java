package com.metapx.local_client.commands;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.metapx.git_metadata.core.MetadataRepository;
import com.metapx.git_metadata.pictures.Picture;
import com.metapx.local_client.cli.ClientEnvironment;
import com.metapx.local_client.cli.Console;
import com.metapx.local_client.daemon.VertxHelper;
import com.metapx.local_picture_repo.scaling.Dimension;
import com.metapx.local_picture_repo.scaling.Dimensions;
import com.metapx.local_picture_repo.verticles.PictureVerticle;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.Vertx;
import rx.Observable;

public class PictureGroup {

  @Command(name = "show",
    description = "Show picture details",
    groupNames = "picture")
  public static class ShowCommand extends CommonCommand {
    @Arguments(title = "picture")
    @Required
    private List<String> pictures;
    
    @Option(name = { "-d", "--dimension" },
            title = "dimension")
    private String dimensionName = "original";
    
    @Override
    public void run(ClientEnvironment env) throws Exception {
      final VertxHelper vertxHelper = new VertxHelper(env.getConfiguration());
      vertxHelper.deployScalingVerticle();
      vertxHelper.deployPictureVerticle();
      
      try {
        final Optional<Dimension> dimension = readDimension();
        final MetadataRepository repo = env.getMetadataRepositoryOrThrow();
        final Console console = env.getConsole();
        
        final Observable<MaterializedPicture> result = Observable.from(pictures)
          .flatMap(pictureHash -> getFile(pictureHash, dimension, vertxHelper.vertx(), repo)
            .observeOn(RxHelper.scheduler(vertxHelper.vertx()))
            .map(path -> new MaterializedPicture(pictureHash, new File(path)))
          )
          .share();
        
        // Emit results immediately when they arrive.
        console.reportMaterializedPictures(result);
        
        // Wait for the observable to complete.
        result.toBlocking().subscribe();
      } finally {
        vertxHelper.vertx().close();
      }
    }
    
    private Observable<String> getFile(String pictureHash,
                                       Optional<Dimension> dimension,
                                       Vertx vertx,
                                       MetadataRepository repo) {
      return Observable.just(pictureHash)
        .doOnNext(hash -> System.out.println(hash))
        .map(hash -> repo.pictures().findWithKey(hash))
        .doOnNext(pictureOpt -> {
          if (!pictureOpt.isPresent()) {
            throw new ItemException("Picture '" + pictureHash + "' not found.");
          }
        })
        .map(pictureOpt -> pictureOpt.get())
        .map(picture -> picture.files()
          .stream()
          .filter(member -> member.getRole() == Picture.Role.ROOT)
          .findFirst()
          .map(root -> root.getFileHash())
          .orElseThrow(() -> new ItemException("No root file found for picture '" + pictureHash + "'"))
        )
        .map(fileHash -> {
          final JsonObject request = new JsonObject()
            .put("hash", fileHash);
          
          if (dimension.isPresent()) {
            request.put("dimension", dimension.get().toJson());
          }
          
          return request;
        })
        .flatMap(request -> vertx.eventBus()
            .<String>rxSend(PictureVerticle.FILEHASH_TO_PATH, request)
            .toObservable()
            .map(message -> message.body())
        );
    }
    
    private Optional<Dimension> readDimension() {
      if (dimensionName.equalsIgnoreCase("original")) {
        return Optional.empty();
      } else {
        return Optional.of(Dimensions.valueOf(dimensionName.toUpperCase()));
      }
    }
  }
  
  /**
   * Represents a picture that was resolved to an image file representing the original image or a scaled version. 
   */
  public static class MaterializedPicture {
    private final String pictureHash;
    private final File file;

    public MaterializedPicture(String pictureHash, File file) {
      super();
      this.pictureHash = pictureHash;
      this.file = file;
    }

    public String getPictureHash() {
      return pictureHash;
    }

    public File getFile() {
      return file;
    }
    
    @Override
    public String toString() {
      return pictureHash + " -> " + file.getAbsolutePath();
    }
  } 
}
