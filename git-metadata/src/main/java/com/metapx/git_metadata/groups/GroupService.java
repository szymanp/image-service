package com.metapx.git_metadata.groups;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.metapx.git_metadata.core.HashPath;
import com.metapx.git_metadata.core.HashPathTransactionElement;
import com.metapx.git_metadata.core.IdService;
import com.metapx.git_metadata.core.TransactionControl;
import com.metapx.git_metadata.core.collections.Collection;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService;
import com.metapx.git_metadata.references.ReferenceService.Operation;
import com.metapx.git_metadata.references.Zone;

public class GroupService {
  private final GroupTreeCollection tree;
  private final IdService idService;
  private final ProviderMap providers = new ProviderMap();
  private final TransactionControl transaction;
  private final HashPathTransactionElement<MemberPictureCollection> pictureCollections;

  public GroupService(File root, TransactionControl transactionControl, IdService idService, ReferenceService refService) {
    this.idService = idService;
    transaction = transactionControl;
    tree = new GroupTreeCollection(new File(root, "tree"), transaction);

    pictureCollections = new HashPathTransactionElement<MemberPictureCollection>(
      new HashPath(root),
      target -> new MemberPictureCollection(target.getFile(), refService, new GroupReference(target.getHash()))
    );
    transaction.addElementToTransaction(pictureCollections);

    final Group.Api api = new Group.Api() {
      public GroupCollection<Group> groups() {
        return new GroupCollection.Complete(tree, providers);
      }
      public GroupCollection<Group> subgroups(Group parent) {
        return new GroupCollection.Subgroups(tree, providers, parent);
      }
  		public Collection<PictureReference> pictures(String groupid) {
        return pictureCollections.get(groupid);
      }
    };

    refService.messages(PictureReference.class, GroupReference.class)
      .filter(m -> m.operation() == Operation.REFERENCE || m.operation() == Operation.UNREFERENCE)
      .filter(m -> !Zone.getCurrent().inZone("group-zone"))
      .subscribe(m -> {
        groups().findWithKey(m.target().getObjectId())
          .ifPresent(group -> {
            if (m.operation() == Operation.REFERENCE) {
              group.pictures().append(m.source());
            } else {
              group.pictures().remove(m.source());
            }
          });
      });

    addProvider(Tag.class, new TagProvider(api));
    addProvider(Device.class, new DeviceProvider(api));
    addProvider(DeviceFolder.class, new DeviceFolderProvider(api));
    addProvider(Place.class, new PlaceProvider(api));
    addProvider(Person.class, new PersonProvider(api));
    addProvider(Event.class, new EventProvider(api));
  }

  public <T extends Group> GroupCollection<T> groups(Class<T> clazz) {
    return new GroupCollection.Typed<T>(tree, providers.get(clazz));
  }

  public GroupCollection<Group> groups() {
    return new GroupCollection.Complete(tree, providers);
  }

  public <T extends Group> T create(Class<T> clazz, String name) {
    return providers.get(clazz).newInstance(idService.createId("group"), name);
  }

  /**
   * Finds a group using a path.
   */
  public Optional<Group> findGroupByPath(String[] path) {
    if (path.length == 0) {
      return Optional.empty();
    }

    Optional<Group> target = groups().findByName(path[0]);
    for(int i=1;i<path.length;i++) {
      if (!target.isPresent()) break;
      target = target.get().subgroups().findByName(path[i]);
    }
    return target;
  }
  
  public Map<String, Class<? extends Group>> getGroupTypes() {
    final Map<String, Class<? extends Group>> types = new HashMap<String, Class<? extends Group>>();
    providers.map.keySet().forEach(clazz -> {
      @SuppressWarnings("unchecked")
      final Class<? extends Group> castClazz = (Class<? extends Group>) clazz;
      types.put(providers.map.get(clazz).getName(), castClazz);
    });
    return types;
  }
  
  protected <T extends Group> void addProvider(Class<T> clazz, GroupProvider<T> provider) {
    providers.put(clazz, provider);
    transaction.addElementToTransaction(provider);
  }

  interface Providers {
    GroupProvider<Group> get(GroupTreeRecord key);
    GroupProvider<Group> get(Group group);
  }

  static class ProviderMap implements Providers {
    final private Map<Class<?>, GroupProvider<?>> map = new HashMap<Class<?>, GroupProvider<?>>();

    public <T extends Group> void put(Class<T> key, GroupProvider<T> value) {
      map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T extends Group> GroupProvider<T> get(Class<T> key) {
      if (!map.containsKey(key)) {
        throw new RuntimeException("No provider for class " + key.getSimpleName());
      }
      return GroupProvider.class.cast(map.get(key));
    }

    @SuppressWarnings("unchecked")
    public GroupProvider<Group> get(GroupTreeRecord key) {
      final Optional<GroupProvider<?>> provider = map.values().stream().filter(p -> p.matches(key)).findFirst();
      if (provider.isPresent()) {
        return (GroupProvider<Group>) provider.get();
      }
      throw new RuntimeException("No provider for type " + key.getType());
    }

    @SuppressWarnings("unchecked")
    public GroupProvider<Group> get(Group group) {
      final Optional<GroupProvider<?>> provider = map.values().stream().filter(p -> p.matches(group)).findFirst();
      if (provider.isPresent()) {
        return (GroupProvider<Group>) provider.get();
      }
      throw new RuntimeException("No provider for type " + group.getType());
    }
  }
}
