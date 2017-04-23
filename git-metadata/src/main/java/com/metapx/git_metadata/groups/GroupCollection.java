package com.metapx.git_metadata.groups;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.groups.GroupService.Providers;

class GroupCollection {
  final KeyedCollection<String, GroupTreeRecord> tree;
  
  private GroupCollection(KeyedCollection<String, GroupTreeRecord> tree) {
    this.tree = tree;
  }
  public void append(Group element) {
    tree.append(GroupTreeRecord.fromGroup(element));
  }
  public void update(Group element) {
    tree.update(GroupTreeRecord.fromGroup(element));
  }
  public void remove(Group element) {
    tree.remove(GroupTreeRecord.fromGroup(element));
  }
  public boolean contains(Group element) {
    return tree.contains(GroupTreeRecord.fromGroup(element));
  }

  abstract static class Untyped implements KeyedCollection<String, Group> {
    final protected GroupCollection inner;
    final protected Providers providers;
    final protected Predicate<GroupTreeRecord> filter;

    Untyped(KeyedCollection<String, GroupTreeRecord> tree, Providers providers, Predicate<GroupTreeRecord> filter) {
      inner = new GroupCollection(tree);
      this.providers = providers;
      this.filter = filter;
    }

    public Optional<Group> findWithKey(String key) {
      return inner.tree.findWithKey(key)
        .filter(record -> filter.test(record))
        .map(treeRecord -> providers.get(treeRecord).readInstance(treeRecord));
    }
    public void append(Group element) {
      inner.append(element);
    }
    public void update(Group element) {
      inner.update(element);
    }
    public void remove(Group element) {
      inner.remove(element);
    }
    public boolean contains(Group element) {
      return inner.contains(element);
    }
    public List<Group> list() {
      return stream().collect(Collectors.toList());
    }
    public Stream<Group> stream() {
      return inner.tree.stream()
        .filter(record -> filter.test(record))
        .map(record -> providers.get(record).readInstance(record));
    }
  }

  /**
   * A collection that lists all groups.
   */
  static class Complete extends Untyped  {
    Complete(KeyedCollection<String, GroupTreeRecord> tree, Providers providers) {
      super(tree, providers, record -> true);
    }

    public void append(Group element) {
      super.append(element);
      providers.get(element).save(element);
    }
    public void update(Group element) {
      inner.tree.update(GroupTreeRecord.fromGroup(element));
      providers.get(element).save(element);
    }
    public void remove(Group element) {
      inner.tree.remove(GroupTreeRecord.fromGroup(element));
      providers.get(element).delete(element);
    }
  }

  /**
   * A collection that lists all subgroups of a given group.
   */
  static class Subgroups extends Untyped {
    final private Group parent;

    Subgroups(KeyedCollection<String, GroupTreeRecord> tree, Providers providers, Group parent) {
      super(tree, providers, record -> record.getParentHash().equals(parent.getId()));
      this.parent = parent;
    }

    public void append(Group element) {
      element.setParent(parent);
      super.append(element);
    }
    public void remove(Group element) {
      element.clearParent();
      providers.get(element).save(element);
    }
    public boolean contains(Group element) {
      return element.getParent().isPresent() && element.getParent().get().equals(parent);
    }
  }

  /**
   * A collection that lists all groups but only of a certain type.
   */
  static class Typed<T extends Group> implements KeyedCollection<String, T> {
    final protected GroupCollection inner;
    final private GroupProvider<T> provider;

    Typed(KeyedCollection<String, GroupTreeRecord> tree, GroupProvider<T> provider) {
      this.inner = new GroupCollection(tree);
      this.provider = provider;
    }

    public Optional<T> findWithKey(String key) {
      return inner.tree.findWithKey(key)
        .filter(record -> provider.matches(record))
        .map(treeRecord -> provider.readInstance(treeRecord));
    }
    public void append(T element) {
      inner.append(element);
      provider.save(element);
    }
    public void update(T element) {
      inner.update(element);
      provider.save(element);
    }
    public void remove(T element) {
      inner.remove(element);
      provider.delete(element);
    }
    public boolean contains(T element) {
      return inner.contains(element);
    }
    public List<T> list() {
      return stream().collect(Collectors.toList());
    }
    public Stream<T> stream() {
      return inner.tree.stream()
        .filter(record -> provider.matches(record))
        .map(record -> provider.readInstance(record));
    }
  }
}
