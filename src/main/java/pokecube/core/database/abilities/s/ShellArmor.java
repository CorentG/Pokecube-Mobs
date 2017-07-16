package pokecube.core.database.abilities.s;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class ShellArmor extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if(move.pre && mob == move.attacked)
        {
            move.criticalLevel = -1;
        }
    }
}
