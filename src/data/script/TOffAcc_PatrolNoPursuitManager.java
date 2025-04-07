package data.script;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;

import data.ids.TOffAcc_RelevantConstants;
import data.utils.TOffAcc_utils;

import java.util.Map;

public class TOffAcc_PatrolNoPursuitManager extends BaseCampaignEventListener implements EveryFrameScript
{
    @Override
    public boolean isDone()
    {
        return false;
    }
    @Override
    public boolean runWhilePaused()
    {
        return false;
    }
    @Override
    public void advance(float amount)
    {

    }

    public TOffAcc_PatrolNoPursuitManager()
    {
        super(true);

    }

    public static TOffAcc_PatrolNoPursuitManager getListener()//modified from exerelin
    {
        Map<String, Object> data = Global.getSector().getPersistentData();
        return (TOffAcc_PatrolNoPursuitManager)data.get(TOffAcc_RelevantConstants.PATROL_LISTENER_MAP_KEY);
    }
    public static TOffAcc_PatrolNoPursuitManager create()//modified from exerelin
    {
        TOffAcc_PatrolNoPursuitManager listener=getListener();
        if (listener!=null)return listener;

        Map<String, Object> data = Global.getSector().getPersistentData();
        listener = new TOffAcc_PatrolNoPursuitManager();
        data.put(TOffAcc_RelevantConstants.PATROL_LISTENER_MAP_KEY, listener);
        return listener;
    }

    public void reportFleetSpawned(CampaignFleetAPI fleet)
    {
        if (fleet==null)return;
        if (fleet.getFaction()==null)return;
        if (!fleet.getFaction().getMemoryWithoutUpdate().getBoolean(TOffAcc_RelevantConstants.TOFF_GRANTED))return;
        TOffAcc_utils.addTOffAccFleet(fleet, fleet.getFaction());
    }
}
