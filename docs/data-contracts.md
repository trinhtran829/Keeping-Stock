# Keeping Stock App – Data Contract and Business Rule

The purpose of this document is to define the data model and business rules for the app. 
It serves as a contract between the Data Lead and other team members to ensure consistency in UX.

This version prioritizing Minimal Viable Product (MVP). More advanced behaviors may be considered as stretch goals.
 
## Core Entities
- Container: A space that can hold items and sub-containers
- Item: An object we are tracking
- Tag: A global and reusable label.
- Item-Tag: Many-to-many relationship between Item and Tag
-----------------------------------------------------------------------------------------
## Container Rules

### Hierarchy
- Each container has at most one parent
- Root containers have parentContainerId = null
- To not form a cycle, a container:
  - May not be moved into itself
  - May not be moved into any of its descendant containers

### Creation
- Upon creation, users may select a parent container
- If no parent container selected, parentContainerId = null

### Edition
- When moving a container (re-parenting):
  - Moving a container updates only that container's parentContainerId
  - Items and sub-containers remain attached to the moved container.
  - Must validate:
    - Not moving into itself
    - Not moving into any descendant container
- Name, Description, and Image can be edited at will
 
### Deletion
- Deletion is allowed only when the current container is empty
- Cannot delete a non-empty container
- An empty container means
    - No items have this containerId
    - No containers have this current container as a parent

---------------------------------------------------------------------------------------------
## Item Rules

### Item-Container Relationship
- One item may belong to 0 or 1 container

### Creation
- Upon creation, users can select a container or leave null
- For simplicity, the following rules are strict upon creation. User can chose to edit later.
- Item with containerId = null: 
  - ItemStatus = TAKEN_OUT, (cannot be STORED)
  - checkoutDate = current date
- Item with containerId != null: 
  - ItemStatus = STORED, (cannot be TAKEN_OUT)
  - checkoutDate = null

### Edition
- Editable fields:
  - Name
  - description
  - image
  - container
  - tag
  - Item Status

- There are 2 types of edit:
  1. Edit status only (NO modifying container):
    - Changing item status from STORED to TAKEN_OUT:
      - checkoutDate = current date
    - Changing item status from TAKEN_OUT to STORED:
      - checkoutDate = null
  2. Edit everything else EXCEPT status (this include container)
     - For simplicity, the following rules are strict upon changing container
     - Item with containerId changed to null:
         - ItemStatus = TAKEN_OUT, (cannot be STORED)
         - checkoutDate remain the same to keep the historical data of the real checkoutDate
     - Item with containerId changed to a specific container:
         - Item status remain the same
         - checkoutDate remain the same
         - Everything else remain the same unless user want to edit
 
### Deletion
- No deletion restriction
- Delete an item will remove the associated rows in ItemTag

---------------------------------------------------------------------------------------------
## Tag Rules
    
### Characteristics
- Tags are global and reusable
- Tag names will be:
    - Trim leading and trailing whitespace
    - Collapse multiple internal spaces into a single space
    - Compare names case-insensitively
    - Allowed characters: 
      - Letters (A-Z)
      - Numbers (0-9)
      - Spaces
      - Hyphen (-)
      - Ampersand (&)
    
#### Creation
- Can be created when add/edit item
- If a normalized tag already exists:
  - The existing tag will be reused
  - A duplicate will not be created
    
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
