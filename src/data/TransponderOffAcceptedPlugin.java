package data;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import data.ids.TOffAcc_RelevantConstants;
import data.script.TOffAcc_LunaSettingsListener;
import data.script.TOffAcc_PatrolNoPursuitManager;
import data.utils.TOffAcc_utils;
import lunalib.lunaSettings.LunaSettings;

import java.util.List;

public class TransponderOffAcceptedPlugin extends BaseModPlugin
{
    private static SectorAPI sector;

    @Override
    public void onApplicationLoad()
    {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            LunaSettings.addSettingsListener(new TOffAcc_LunaSettingsListener());
        }
    }

    @Override
    public void onGameLoad(boolean newGame)
    {
        sector=Global.getSector();
        addScripts();
        if (!TOffAcc_utils.modIsPreviouslyEnabled_1())
        {
            sector.getMemory().set(TOffAcc_RelevantConstants.TOFFACC_IS_PREVIOUSLY_ENABLED_1,true);
            initFac();
        }
    }

    private void addScripts()
    {
        sector.addScript(TOffAcc_PatrolNoPursuitManager.create());
    }

    private void initFac()
    {
        List<FactionAPI> factions=sector.getAllFactions();
        for (FactionAPI faction : factions)
        {
            if (faction.getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)&&faction.getCustomBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE))
            {
                faction.getMemory().set(TOffAcc_RelevantConstants.TOFFACC_NOT_SUITABLE,true);
            }
            else faction.getMemory().set(TOffAcc_RelevantConstants.TOFFACC_NOT_SUITABLE,false);
        }
    }
}