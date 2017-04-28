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
import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.pictures.PictureReference;
import com.metapx.git_metadata.references.ReferenceService;

public class GroupService {
  private final GroupTreeCollection tree;
  private final IdService idService;
  private final ReferenceService refService;
  private final ProviderMap providers = new ProviderMap();
  private final TransactionControl transaction;
  private final HashPathTransactionElement<MemberPictureCollection> pictureCollections;

  public GroupService(File root, TransactionControl transactionControl, IdService idService, ReferenceService refService) {
    this.idService = idService;
    this.refService = refService;
    transaction = transactionControl;
    tree = new GroupTreeCollection(new File(root, "tree"), transaction);

    pictureCollections = new HashPathTransactionElement<MemberPictureCollection>(
      new HashPath(root),
      target -> new MemberPictureCollection(target.getFile())
    );
    transaction.addElementToTransaction(pictureCollections);

    final Group.Api api = new Group.Api() {
      public KeyedCollection<String, Group> groups() {
        return new GroupCollection.Complete(tree, providers);
      }
      public Collection<Group> subgroups(Group parent) {
        return new GroupCollection.Subgroups(tree, providers, parent);
      }
  		public Collection<PictureReference> pictures(String groupid) {
        return pictureCollections.get(groupid);
      }
    };

    addProvider(Tag.class, new TagProvider(api));
  }

  public <T extends Group> KeyedCollection<String, T> groups(Class<T> clazz) {
    return new GroupCollection.Typed<T>(tree, providers.get(clazz));
  }

  public KeyedCollection<String, Group> groups() {
    return new GroupCollection.Complete(tree, providers);
  }

  public <T extends Group> T create(Class<T> clazz, String name) {
    return providers.get(clazz).newInstance(idService.createId("group"), name);
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
