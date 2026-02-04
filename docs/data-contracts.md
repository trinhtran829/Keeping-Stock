# Keeping Stock App – Data Contract and Business Rule

The purpose of this document is to define the data model and business rules for the app. 
It serves as a contract between the Data Lead and other team members to ensure consistency in UX.
 
## Core Entities
- Container: A space that can hold items and sub-containers
- Item: An object we are tracking
- Tag: A global and reusable label.
- Item-Tag: Many-to-many relationship between Item and Tag
-----------------------------------------------------------------------------------------
## Container Rules

### Hierarchy
- Each container has at most one parent.
- Must not form a cycle
- Root containers have parentContainerId = null

### Creation
- When created outside of a container: parentContainerId defaults to null
- When created inside of a container: parentContainerId defaults to the current container
- Users cannot select an alternate parent during creation

### Edition
- When moving a container (re-parenting), this action will move all items and sub-containers 
inside the current container to a new parent container.
- Name, Description, and Image can be edited at will.
 
### Deletion
- Deletion is allowed only when the current container is empty
- Cannot delete a non-empty container
- An empty container means
    - No items have this containerId
    - No containers have this current container as a parent.

---------------------------------------------------------------------------------------------
## Item Rules

### Item-Container Relationship
- One item may belong to 0 or 1 container

### Creation
- When created outside of a container, users can select a container or leave null
- Item with containerId = null: ItemStatus = TAKEN_OUT, checkoutDate = creationDate
- Item with containerId = not null: ItemStatus = STORED, checkoutDate = null
- When created inside of a container: containerId defaults to the current container
- Users cannot select an alternate container during creation inside a container

### Edition
- Name, description, image, container, tag, and Item Status can be edited at will.
- Changing item status from STORED to TAKEN_OUT will automatically mark checkoutDate 
as the current date.
 
### Deletion
-  No deletion restriction

---------------------------------------------------------------------------------------------
## Tag Rules
    
### Characteristics
- Tags are global and reusable
- Tag names will be:
    - trimmed
    - case-insensitive to maintain uniqueness
    - Eg: Winter, WINTER, and winter are the same
    
#### Creation
- Can be created when add/edit item
- Existing tags will be reused
- Not allowed to create duplicate tags
    
### Tag-Item Relationship
- One item can have 0 or many tags
- Removing a tag from an item will:
- Delete the row from item_tag
- Does not delete the tag itself
    
I know this is UI territory, but I imagine you will set up something like a drop-down menu 
inside Item Browser to display all possible sorting tags and their number of 
associated items, and this is also where you might set 
the add/edit/delete button for tags. This visualization helps me create the rules below:
    
    
### Edition
- Renaming a tag will affect all items using that tag.
- Not allowed to rename to an already existing tag.
    
### Deletion
- Tags associated with items cannot be deleted.
- Only allow deletion for tags that are not in use 
