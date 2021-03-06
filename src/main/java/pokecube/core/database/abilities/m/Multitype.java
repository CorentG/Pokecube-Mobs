package pokecube.core.database.abilities.m;

import net.minecraft.item.ItemStack;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.lib.CompatWrapper;

public class Multitype extends Ability
{
    @Override
    public void onUpdate(IPokemob mob)
    {
        PokedexEntry entry = mob.getPokedexEntry();

        if (!entry.getName().contains("Arceus")) return;
        ItemStack held = mob.getHeldItem();
        if (CompatWrapper.isValid(held) && held.getItem().getRegistryName().getResourceDomain().contains("pokecube")
                && held.getItem().getRegistryName().getResourcePath().contains("badge") && held.hasTagCompound())
        {
            String name = held.getTagCompound().getString("type");
            String typename = name.replace("badge", "");
            PokeType type = PokeType.getType(typename);
            if (type != PokeType.unknown)
            {
                mob.setPokedexEntry(Database.getEntry("arceus" + type));
                return;
            }
        }
        if (entry.getBaseForme() != null && entry.getBaseName().equals("Arceus"))
        {
            mob.setPokedexEntry(entry.getBaseForme());
            return;
        }

    }
}
