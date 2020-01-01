# Hello there!
### Welcome to my notes about MC modding/Fabric. This is where I write down any ideas/thoughts I have that are remotely related to modding. I'm making this "open source" per-sé so that anyone and everyone can steal my ideas if they wish.

### In the future, I'd like to set up something somewhat similar to this, but for the entire community (but a bit more organized!). Essentially, I'd like to create an up-to-date list of everyone's active projects and potential future plans. This would enable quick access of information, without needing to scroll through days of Discord messages. One of the chief goals of this would help prevent duplicate mods from being created & to increase code reuse in the community (via better access to (and  awareness of) libraries for new mod developers).

# General Mod/Modding Ideas
  * Server-side system for servers running modpacks to automatically update the component mods
  * Allow crafting table to access the inventories of items in your inventory (Requires them to use the same inventory data structure)
  * MC suffers from a fundamental issue in its progression, imo. Everything is tied to mining, and the only way (in vanilla, and most mods) to progress further is to simply mine for hours, hoping that RNG is in your favor and you can find that rare material. This works, but only to a point. A new progression method is needed (And there has to be a reason to get the stuff). SOLUTIONS:
    - Stuff like skill trees/character levels
    - Bosses with gear drops, or resources for powerful gear
    - Quests for gear, or to custom dimensions with new types of resources or mechanics
    - Anything that changes what is needed (now, maybe, armor matters less in this one area, but a different type of item, skill, level, pet, etc is needed to progress).
    - One thing to keep in mind with all of this is that acquiring this next tier in the progression system should be worthwhile. No one wants to go through all that trouble to get something that is just a reskin of diamond armor. Often, I think it is a good idea to add a novel feature to the things. (EX: set bonuses for armor, weapon "abilities", etc.) This method also has the added bonus of removing clear "best items," as its no longer just a stats fest. Adding utility and novelty to items is beneficial.
  * Revivable pets, pet upgrades/augments, etc. Custom pet class if one does not exist. Provide method of adding new behaviors. Ridable? Flyable? No friendly fire
  * Allow searching based on item's source mod name in REI
  * Universal config file for things like creative tabs/categories. Mods can implement a default setup, but allow peeps to modify these configs as they see fit. Possibly also/otherwise create an "Item Archetype" tag thing that people can implement in their mods. Perhaps allow items to belong to multiple creative tabs.
  * Craftable (survival obtainable) player heads?
  * Cosmetics generation for unified feel between mods (downside of this is that you potentially lose uniqueness. Counter this by allowing mod devs to create their own armor material categories.) Default: Metal, Gem, Solid, etc. Create a class/color palette system that will generate colorized textures based on inputs.
  * Load lower-quality resource pack for items in inventory, ESPECIALLY/PARTICULARLY IN NEI/REI, as on higher-quality displays, there are TONS of items displayed (depending on GUI scale) which KILLS framerate
  * Add a more complicated/in-depth damage system to Minecraft. Physical/magic damage & resistance.
  * Themed enemy types.
  * Enemies with synergistic abilities?
  * Lava boats, Bigger boast (think Archimedes Ships, but use entities.)
Perhaps make new type of thing, or look at how current boat implementation works. While walking on them, player location should be stored relative to that of the ship I think. Boat location changes are calculated first.) This technology can be used to create multi-person flying ships as well. If we can create an entity composed of multiple component entities, there is a possibility of making even a movable base. Interesting.
  * mod that allows players to add visual customizations to armor/items. (modular, sorta. Applies on top of/in conjunction with the item's base skin.) Gear "attachments" -> with a menu for each armor/weapon piece.
  * Carpets become waterlogged when they encounter water, rather than being broken?
  * Enchantment translator -> transfer enchantments from one item directly to another for a certain amount of experience (regardless of material. Select the enchantments you wish to transfer). (IDK, this might be OP since XP can be made easy to obtain. Possibly require another item to do this)
  * Enchantment particles (visual) special effects for different enchants! (ex, subtle flames when fire aspect)
  * Inventory tools (and AUTO sort inventory, not just a button). Include the ability to specify preferred locations for various items(specific tools, in addition to "type") and item types(resource stacks/other) (save as a file that can be loaded & shared)
  * Ability to set block hardness, resistance, etc as a tag. (to potentially allow players to create "reinforced" blocks? (to be blast resistant) (Ex: want to use quartz for design, but want blast resistance? idk
  * EffectiveStacks -> items in hotbar (*When out of inventory menu?) show the #of the total amount of that item, instead of just that stack. Items from inv are automatically switched to hotbar when needed to make this work effectively.
  * Add a way for custom items to implement textures that already exist (like stone, wood planks, etc) in their own item/block textures, then they can add their modifications on top of them. In this way, you can sort of make these custom blocks compatible with vanilla resource packs. Also allow stacking of existing resources (requires you to create a library of texture components) Example: Nether lapis ore = Netherrack + Lapis AccentTexture/Layer2Texture (something like that)


# Mod Bugs/Issues to report and/or create fixes for
### Nether Things
 * Nether cactus does not drop higher up blocks if lower is broken

### Stockpile
 * Barrels Mod (Stockpile?) bug: for tools/weapons, the enchantments (or lack thereof) of the (first?) item are copied to all other items put into the barrel. As a matter of fact, I think that all items that come from the barrel are just copies of the first. This has the effect of allowing people to duplicate items with strong enchantments, or can inadvertently cause them to lose all of their enchantments if they accidentally put an enchanted item in the barrel of a non-enchanted item.

### "Traps Mod" (Not actual name)
Spike traps from the traps mod are WAY too loud and have their audio coded incorrectly. The sound always plays as if it originates from the player's location.

### SimplePortals
* Add portal cooldowns.
* Change teleport function to support multiple (including custom, ideally) dimensions
* Change Teleport function to teleport to the half-block (x&z) (so that players are teleported to the middle of the block that they select, rather than its NW corner.


# All things Fabric
## Wiki
### Moving to a new platform
I feel that we should think about switching to a more robust & full-featured wiki service. MediaWiki in particular comes to mind, simply because it is already so familiar to many people, and is well established. (With effectively a guarantee of future support).

### If we stick with DocuWiki
 * Probably a good idea to create a template that suits our needs.
    * Categorized navigation sidebar
    * Make use of more screen real-estate. No reason to confine the page. This has the added benefit that adding a sidebar won't kill 1/3-1/4 the available space.
    * Page templates that have builtin "goto code example" things
 * Todo
    * Create reusable components for tutorial pages
    * Perhaps some sort of dynamic sitemap and/or "next page" function for pages that are a part of groups (ex: "Beginner Tutorial")
 * Notes
    * Navigation sidebar for the wiki is sorely needed.

## Uncategorized
 * We need a way of compiling all of our knowledge in one place (that is searchable). Somehow need to get info from discord to a web page. Make it as easy as possible for people to adopt standards and share/reuse code.
 * Similar to previous, we need a better "new fabric developer" tutorial suite. We should also definitely introduce some of the most often used APIs so that we can get everyone up and running as quickly as possible, and also encourage some innate interoperability between mods.


# Random Uncategorized
