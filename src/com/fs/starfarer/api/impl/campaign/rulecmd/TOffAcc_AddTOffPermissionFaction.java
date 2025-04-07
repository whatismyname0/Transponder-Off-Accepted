package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import data.utils.TOffAcc_utils;

public class TOffAcc_AddTOffPermissionFaction extends BaseCommandPlugin
{
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if (dialog==null)return false;
        FactionAPI fac=dialog.getInteractionTarget().getActivePerson().getFaction();
        if (fac!=null)
        {
            TOffAcc_utils.addTOffAccFaction(fac);
        }
        return true;
    }
}