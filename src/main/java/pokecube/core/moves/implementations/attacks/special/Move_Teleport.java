package pokecube.core.moves.implementations.attacks.special;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Move_Teleport extends Move_Basic
{
    public Move_Teleport()
    {
        super("teleport");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        IPokemob attacker = packet.attacker;
        Entity attacked = packet.attacked;
        Entity target = attacker.getEntity().getAttackTarget();
        if (attacked == attacker.getEntity() && target != null) attacked = target;
        if (attacked instanceof EntityLiving)
        {
            ((EntityLiving) attacked).setAttackTarget(null);
        }
        if (attacker.getPokemonAIState(IMoveConstants.TAMED))
        {
            attacker.returnToPokecube();
        }
        super.postAttack(packet);
    }
}
