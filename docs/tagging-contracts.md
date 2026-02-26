# Tag Contract 

Bolded options are assumed behavior so far

## Tag scope and behavior

What can have tags?
- **Tags only on Items**
- Only on Containers
- Both

Can Container tags be automatically applied to subcontainers/items
- inherit
- no inheritance
- **N/A**

## Persistence timing and reuse

When tags are edited during CREATE/EDIT mode, when should the tag changes be applied?
- Immediately
- **Defer until save button is pushed**

Duplicate tags?
- Creat duplicates (local tagging)
- **Reuse existing (global tagging)**

## Global tag management

Can tags be removed or renamed globally from any item's Add/Edit screen?
- Rename/delete tags allowed from Add/Edit 
- **Only modify tag-item associations - do NOT allow rename/delete tags globally**

## Limits and ordering

Are there a max number of tags an item can have?
- Unlimited 
- **Capped (I arbitrarily set max to 20 per item - can be easily edited from a single variable)**

How should selected tags be ordered?
- **Alphabetical**
- Most recent

## Editing UX

Where should editing the tags for an item take place?
- **Inline (a separate elevated card inside the AddEdit screen)**
- Open overlay when Edit Tags button is pressed
- Separate screen when Edit Tags button is pressed

Current inline sections:
- Selected tags shown as chips with X icon for removal.
- Input text field for searching/creating tags.
- Suggestions list (top x amount - x is an easily adjust value, currently set at 8).
- Always offer "Add <currentQuery>" if valid and not already selected.

## Recommendations UX

When should recommendations be retrieved?
- Manual only
- Auto-run after image pick
- **Auto-run after image pick + manual refresh button option at any other time**

Recommendations are sourced based on
- **Input image only**
- Image + text (name/description)

When recommended chip is tapped:
- Confirm before applying tag to item
- **Tap adds recommended tag to item immediately (x can remove it)**

## Validation and normalization

Where does normalization happen?
- UI
- **ViewModel**
- Repo

How are invalid character handled?
- Stripped silently/automatically. Tag is not added.
- **Rejected using inline error notificaiton around the text field. Tag is not added**

Normalization rules so far:
- Trim leading/trailing whitespace
- Collapse multiple internal spaces to one
- Compare case-insensitively for uniqueness
- Allowed chars only: letters A-Z, digits 0-9, space, hyphen -, ampersand &
- Storage assumption (selected):
- Keep a display name as entered (or lightly cleaned), but enforce uniqueness using a normalized key (lowercased, collapsed spaces). (Exact DB approach can be decided by data lead; UI will rely on ViewModel outputs.)

## Dirty tracking

Do tag edits affect if the form is considered dirty for navigation purposes?
- **Yes**
- No

## ML Kit labeler: UI vs ViewModel responsibility

Assumed approach: ViewModel responsibility to pull recommendations. UI can only launch camera/picker and forward results (URI).

ViewModel/domain should:

- call the ML Kit labeler
- map labels -> tag candidates
- emit them as recommendedTags in UiState
- handle loading/error states and deduping with existing tags