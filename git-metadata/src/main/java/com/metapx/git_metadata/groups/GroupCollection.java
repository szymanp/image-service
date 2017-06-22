package com.metapx.git_metadata.groups;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metapx.git_metadata.core.collections.KeyedCollection;
import com.metapx.git_metadata.groups.GroupService.Providers;

/**
 * A collection of groups.
 *
 * @param <T> The type of groups available in this collection.
 */
public interface GroupCollection<T extends Group> extends KeyedCollection<String, T> {
  
  /**
   * Finds a group by its name.
   * @param name
   */
  public Optional<T> findByName(String name);
  
  /**
   * Finds a group by an id prefix.
   * @param idPrefix
   * @return a group if it was uniquely identified by this prefix, otherwise an empty optional.
   */
  public Optional<T> findByIdPrefix(String idPrefix);
  
  static class Base {
    final KeyedCollection<String, GroupTreeRecord> tree;
    
    private Base(KeyedCollection<String, GroupTreeRecord> tree) {
      this.tree = tree;
    }
    public void append(Group element) {
      validate(element);
      tree.append(GroupTreeRecord.fromGroup(element));
    }
    public void update(Group element) {
      validate(element);
      tree.update(GroupTreeRecord.fromGroup(element));
    }
    public void remove(Group element) {
      tree.remove(GroupTreeRecord.fromGroup(element));
    }
    public boolean contains(Group element) {
      return tree.contains(GroupTreeRecord.fromGroup(element));
    }
    
    private void validate(Group element) {
      // Validate the name
      if (element.getName().equals("")) {
        throw new GroupException.InvalidNameException(element.getName());
      }

      // Make sure that no subgroup with the same name exists.
      final GroupTreeRecord validated = GroupTreeRecord.fromGroup(element);
      final Optional<GroupTreeRecord> sameName = tree.stream()
        .filter(record -> record.getParentHash().equals(validated.getParentHash())
                          && record.getName().equals(validated.getName()))
        .findAny();
      if (sameName.isPresent()) {
        throw new GroupException.AlreadyExistsException(element.getName());
      }
    }
  }

  abstract static class Untyped implements GroupCollection<Group> {
    final protected Base inner;
    final protected Providers providers;
    final protected Predicate<GroupTreeRecord> filter;

    Untyped(KeyedCollection<String, GroupTreeRecord> tree, Providers providers, Predicate<GroupTreeRecord> filter) {
      inner = new Base(tree);
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
    public Optional<Group> findByName(String name) {
      return inner.tree.stream()
        .filter(record -> filter.test(record))
        .filter(record -> record.getName().equals(name))
        .map(record -> providers.get(record).readInstance(record))
        .findFirst();
    }
    public Optional<Group> findByIdPrefix(String idPrefix) {
      final String prefix = idPrefix.toLowerCase();
      final List<GroupTreeRecord> elements = inner.tree.stream()
        .filter(record -> filter.test(record))
        .filter(record -> record.getGroupHash().startsWith(prefix))
        .limit(2)
        .collect(Collectors.toList());
      if (elements.size() == 1) {
        return elements.stream()
          .map(record -> providers.get(record).readInstance(record))
          .findFirst();
      } else {
        return Optional.empty();
      }
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
  static class Typed<T extends Group> implements GroupCollection<T> {
    final protected Base inner;
    final private GroupProvider<T> provider;

    Typed(KeyedCollection<String, GroupTreeRecord> tree, GroupProvider<T> provider) {
      this.inner = new Base(tree);
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
    public Optional<T> findByName(String name) {
      return inner.tree.stream()
        .filter(record -> provider.matches(record))
        .filter(record -> record.getName().equals(name))
        .map(record -> provider.readInstance(record))
        .findFirst();
    }
    public Optional<T> findByIdPrefix(String idPrefix) {
      final String prefix = idPrefix.toLowerCase();
      final List<GroupTreeRecord> elements = inner.tree.stream()
        .filter(record -> provider.matches(record))
        .filter(record -> record.getGroupHash().startsWith(prefix))
        .limit(2)
        .collect(Collectors.toList());
      if (elements.size() == 1) {
        return elements.stream()
          .map(record -> provider.readInstance(record))
          .findFirst();
      } else {
        return Optional.empty();
      }
    }
  }
}
