package data.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import data.ids.TOffAcc_RelevantConstants;

import java.util.List;

public class TOffAcc_utils
{
    public static void addTOffAccFaction(FactionAPI faction)
    {
        List<StarSystemAPI> systems= Global.getSector().getStarSystems();
        for (StarSystemAPI system:systems)
        {
            List<CampaignFleetAPI> fleets= Misc.getFleetsInOrNearSystem(system);
            for (CampaignFleetAPI fleet:fleets)
            {
                addTOffAccFleet(fleet,faction);
            }
        }
        faction.getMemory().set(TOffAcc_RelevantConstants.TOFF_GRANTED,true);
    }
    public static void addTOffAccFleet(CampaignFleetAPI fleet,FactionAPI faction)
    {
        if (fleet.getFaction()!=faction)return;
        if (!fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET))return;
        fleet.getMemory().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF,true);
        fleet.getMemory().set("$cfai_allowPlayerBattleJoinTOff",true);
    }
    public static void removeTOffAccFaction(FactionAPI faction)
    {
        List<StarSystemAPI> systems= Global.getSector().getStarSystems();
        for (StarSystemAPI system:systems)
        {
            List<CampaignFleetAPI> fleets= Misc.getFleetsInOrNearSystem(system);
            for (CampaignFleetAPI fleet:fleets)
            {
                removeTOffAccFleet(fleet,faction);
            }
        }
        faction.getMemory().set(TOffAcc_RelevantConstants.TOFF_GRANTED,false);
    }
    public static void removeTOffAccFleet(CampaignFleetAPI fleet,FactionAPI faction)
    {
        if (fleet.getFaction()!=faction)return;
        if (!fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET))return;
        fleet.getMemory().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF,false);
        fleet.getMemory().set("$cfai_allowPlayerBattleJoinTOff",false);
    }

    @Deprecated
    public static boolean modIsPreviouslyEnabled()
    {
        return Global.getSector().getMemoryWithoutUpdate().getBoolean(TOffAcc_RelevantConstants.TOFFACC_IS_PREVIOUSLY_ENABLED);
    }

    public static boolean modIsPreviouslyEnabled_1()
    {
        return Global.getSector().getMemoryWithoutUpdate().getBoolean(TOffAcc_RelevantConstants.TOFFACC_IS_PREVIOUSLY_ENABLED_1);
    }

    static public float computeStipend() {
        float level = Global.getSector().getPlayerPerson().getStats().getLevel();

        return (Global.getSettings().getFloat("factionCommissionStipendBase") +
                Global.getSettings().getFloat("factionCommissionStipendPerLevel") * level)*TOffAcc_RelevantConstants.TOFFACC_STIPEND_RATIO;
    }
}
