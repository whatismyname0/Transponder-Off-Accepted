package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;
import java.util.Map;

import data.ids.TOffAcc_RelevantConstants;
import data.intel.TOffAccIntel;
import data.utils.TOffAcc_utils;

public class TOffAcc extends BaseCommandPlugin
{
    protected FactionAPI entityFaction;
    protected InteractionDialogAPI dialog;
    protected FactionAPI faction;
    protected PersonAPI person;
    protected MarketAPI market;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        this.dialog = dialog;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        SectorEntityToken target=dialog.getInteractionTarget();
        market = target.getMarket();
        entityFaction = target.getFaction();
        person =target.getActivePerson();
        faction = person.getFaction();

        if (entityFaction!=faction) return false;
        if (market == null) return false;

        switch (command)
        {
            case "printRequirements":{printRequirements();return true;}
            case "playerMeetsCriteria":return playerMeetsCriteria();
            case "hasFactionTOffAcc":return hasFactionTOffAcc();
            case "printInfo":{printInfo();return true;}
            case "accept":{accept();return true;}
            case "resign":{resign(true);return true;}
            case "resignNoPenalty":{resign(false);return true;}
            case "personCanGiveTOffAcc":return personCanGiveTOffAcc();
        }
        return true;
    }

    protected boolean hasFactionTOffAcc() {
        return faction.getMemoryWithoutUpdate().getBoolean(TOffAcc_RelevantConstants.TOFF_GRANTED);
    }

    protected boolean playerMeetsCriteria() {
        return faction.getRelToPlayer().isAtWorst(TOffAcc_RelevantConstants.TOFFACC_LEAST_REP);
    }
    protected void printRequirements() {
        CoreReputationPlugin.addRequiredStanding(entityFaction, TOffAcc_RelevantConstants.TOFFACC_LEAST_REP, null, dialog.getTextPanel(), null, null, 0f, true);
        CoreReputationPlugin.addCurrentStanding(entityFaction, null, dialog.getTextPanel(), null, null, 0f);
    }
    protected void printInfo() {
        TooltipMakerAPI info = dialog.getTextPanel().beginTooltip();

        Color h = Misc.getHighlightColor();

        info.setParaSmallInsignia();

        if (TOffAcc_utils.computeStipend()>=0)info.addPara("根据条例,一支登记在案的编外舰队每月收到 %s 的行动资金.", 0f, h, Misc.getDGSCredits((int) TOffAcc_utils.computeStipend()));
        else info.addPara("根据条例,一支登记在案的编外舰队每月会被收取 %s 的登记费用.", 0f, h, Misc.getDGSCredits((int)-TOffAcc_utils.computeStipend()));
        dialog.getTextPanel().addTooltip();
    }
    protected void accept()
    {
        TOffAccIntel intel = new TOffAccIntel(faction);
        intel.missionAccepted();
        intel.sendUpdate(TOffAccIntel.UPDATE_PARAM_ACCEPTED, dialog.getTextPanel());
    }
    protected void resign(boolean withPenalty) {
        TOffAccIntel intelT = null;
        List<IntelInfoPlugin> intels=Global.getSector().getIntelManager().getIntel();
        for (IntelInfoPlugin intel:intels)
        {
            if (!(intel instanceof TOffAccIntel))continue;
            if (((TOffAccIntel) intel).getFaction()==faction)
            {
                intelT=(TOffAccIntel)intel;
                break;
            }
        }
        if (intelT != null) {
            BaseMissionIntel.MissionResult result = intelT.createResignedTOffAccResult(withPenalty, true, dialog);
            intelT.setMissionResult(result);
            intelT.setMissionState(BaseMissionIntel.MissionState.ABANDONED);
            intelT.endMission(dialog);
        }
    }

    protected boolean personCanGiveTOffAcc() {
        if (person == null) return false;
        if (person.getFaction().isPlayerFaction()) return false;
        if (Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
                Ranks.POST_STATION_COMMANDER.equals(person.getPostId()) ||
                Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) ||
                Ranks.POST_OUTPOST_COMMANDER.equals(person.getPostId()) ||
                Ranks.POST_PORTMASTER.equals(person.getPostId()) ||
                person.getMemoryWithoutUpdate().getBoolean(TOffAcc_RelevantConstants.TOFFACC_PERSON_FORCE_PROVIDE))
            return true;
        List<PersonAPI> persons=market.getPeopleCopy();
        for (PersonAPI pers:persons)
            if (otherPersonCanGiveTOffAcc(pers))
                return false;
        if (market.getAdmin().equals(person)) return true;
        person.getMemory().set(TOffAcc_RelevantConstants.TOFFACC_PERSON_FORCE_PROVIDE,true);
        return true;
    }

    protected boolean otherPersonCanGiveTOffAcc(PersonAPI pers) {
        if (pers == null) return false;
        if (pers.getFaction().isPlayerFaction()) return false;
        return Ranks.POST_BASE_COMMANDER.equals(pers.getPostId()) ||
                Ranks.POST_STATION_COMMANDER.equals(pers.getPostId()) ||
                Ranks.POST_ADMINISTRATOR.equals(pers.getPostId()) ||
                Ranks.POST_OUTPOST_COMMANDER.equals(pers.getPostId()) ||
                Ranks.POST_PORTMASTER.equals(person.getPostId()) ||
                pers.getMemoryWithoutUpdate().getBoolean(TOffAcc_RelevantConstants.TOFFACC_PERSON_FORCE_PROVIDE);
    }
}
