package com.metapx.git_metadata.groups;

import java.util.Optional;

import com.metapx.git_metadata.core.collections.Collection;
import com.metapx.git_metadata.pictures.PictureReference;

public abstract class Group {
	private String name;
	private String id;
	private String parentId;
	private Group parent;
	private GroupReference ownReference;
	protected final Api groupApi;

	protected Group(Api api) {
		groupApi = api;
	}

	protected Group(Api api, GroupTreeRecord treeRecord) {
		this(api);
		id = treeRecord.getGroupHash();
		parentId = treeRecord.getGroupHash();
		name = treeRecord.getName();
	}

	protected Group(Api api, String id, String name) {
		this(api);
		this.id = id;
		this.name = name.trim();
	}

	public void setName(String name) { this.name = name.trim(); }
  public String getName() { return name; }
	public String getId() { return id; }
	public abstract String getType();

	public GroupReference getReference() { 
		if (id == null) {
			throw new RuntimeException("This group does not have an id.");
		}
		if (ownReference == null) {
			ownReference = new GroupReference(id);
		}
		return ownReference;
	}

	/**
	 * @return the parent group of this group, if any.
	 */
	public Optional<Group> getParent() {
		if (parentId == null || parentId.equals("")) {
			return Optional.empty();
		} else {
			if (parent == null) {
				final Optional<Group> parentOpt = groupApi.groups().findWithKey(parentId);
				if (!parentOpt.isPresent()) {
					throw new RuntimeException("Group with id '" + parentId + "' not found");
				}
				parent = parentOpt.get();
			}
			return Optional.of(parent);
		}
	}
	/**
	 * Sets the parent group of this group.
	 */
	public void setParent(Group parent) {
		this.parent = parent;
		this.parentId = parent.getId();
	}
	/**
	 * Clears the parent group of this group.
	 */
	public void clearParent() {
		this.parent = null;
		this.parentId = "";
	}

	/**
	 * @return a list of all pictures belonging to this group.
	 */
	public Collection<PictureReference> pictures() {
		return groupApi.pictures(id);
	}

	/**
	 * @return a list of all subgroups of this group.
	 */
	public GroupCollection<Group> subgroups() {
		return groupApi.subgroups(this);
	}

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other != null && other instanceof Group) {
      return id.equals(((Group) other).id);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

	protected void setParent(String parentId) {
		this.parentId = parentId;
		this.parent = null;
	}

	public interface Api {
	  GroupCollection<Group> groups();
    GroupCollection<Group> subgroups(Group group);
		Collection<PictureReference> pictures(String groupid);
	}
}
