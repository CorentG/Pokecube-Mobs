package pokecube.core.moves.implementations.attacks.ongoing;

import java.util.Random;

import net.minecraft.entity.EntityLiving;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.templates.Move_Ongoing;

public class Move_Firespin extends Move_Ongoing
{

    public Move_Firespin()
    {
        super("firespin");
    }

    @Override
    public void doOngoingEffect(EntityLiving mob)
    {
        if (CapabilityPokemob.getPokemobFor(mob).isType(ghost)) return;
        super.doOngoingEffect(mob);
    }

    public int getDuration()
    {
        Random r = new Random();
        return 2 + r.nextInt(4);
    }

}
