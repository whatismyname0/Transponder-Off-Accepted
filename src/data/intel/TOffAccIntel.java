package data.intel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.ids.TOffAcc_RelevantConstants;
import data.utils.TOffAcc_utils;
import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

public class TOffAccIntel extends BaseMissionIntel implements EveryFrameScript, EconomyTickListener {
    public static Logger log = Global.getLogger(TOffAccIntel.class);

    public static String UPDATE_PARAM_ACCEPTED = "update_param_accepted";

    protected FactionAPI faction;

    public TOffAccIntel(FactionAPI faction) {
        this.faction = faction;
    }

    @Override
    public void advanceMission(float amount) {

        RepLevel level = faction.getRelToPlayer().getLevel();
        if (!level.isAtWorst(TOffAcc_RelevantConstants.TOFFACC_LEAST_REVOKE_REP)) {
            setMissionResult(new MissionResult(-1, null));
            setMissionState(MissionState.COMPLETED);
            endMission();
            sendUpdateIfPlayerHasIntel(missionResult, false);
        }
    }


    @Override
    public void missionAccepted() {
        log.info(String.format("确认与 %s 签署特别舰队备案", faction.getDisplayName()));

        setImportant(true);
        setMissionState(MissionState.ACCEPTED);

        Global.getSector().getIntelManager().addIntel(this, true);
        Global.getSector().getListenerManager().addListener(this);
        Global.getSector().addScript(this);
    }

    public List<FactionAPI> getRelevantFactions() {
        Set<FactionAPI> factions = new LinkedHashSet<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isHidden()) continue;
            FactionAPI curr = market.getFaction();
            if (factions.contains(curr)) continue;

            if (curr.isShowInIntelTab()) {
                factions.add(curr);
            }
        }

        return new ArrayList<>(factions);
    }

    public List<FactionAPI> getHostileFactions() {
        List<FactionAPI> hostile = new ArrayList<>();
        for (FactionAPI other : getRelevantFactions()) {
            if (this.faction.isHostileTo(other)) {
                hostile.add(other);
            }
        }
        return hostile;
    }

    @Override
    public void endMission() {
        endMission(null);
    }
    public void endMission(InteractionDialogAPI dialog) {
        log.info(String.format("与 %s 结束了特别舰队备案", faction.getDisplayName()));
        Global.getSector().getListenerManager().removeListener(this);
        Global.getSector().removeScript(this);
        TOffAcc_utils.removeTOffAccFaction(faction);

        endImmediately();
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color c = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        info.addPara(getName(), c, 0f);

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;

        if (getListInfoParam() == UPDATE_PARAM_ACCEPTED) {
            return;
        }

        if (TOffAcc_utils.computeStipend()>=0)info.addPara("%s 每月行动资金", 0f, tc, h, Misc.getDGSCredits(TOffAcc_utils.computeStipend()));
        else info.addPara("%s 每月登记费用", 0f, tc, h, Misc.getDGSCredits(-TOffAcc_utils.computeStipend()));
        unindent(info);
    }

    public String getName() {
        String prefix = Misc.ucFirst(faction.getPersonNamePrefix()) + " 特别舰队备案";
        if (isEnding()) {
            if (missionResult != null && missionResult.payment < 0) {
                if (isSendingUpdate()) {
                    return prefix + " - 已中止";
                }
                return prefix + " - 已中止";
            }
            return prefix + " - 已撤销";
        }
        if (isSendingUpdate() && getListInfoParam() == UPDATE_PARAM_ACCEPTED) {
            return prefix + " - 已接受";
        }
        return prefix;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return faction;
    }

    public FactionAPI getFaction() {
        return faction;
    }

    public String getSmallDescriptionTitle() {
        return getName();
    }

    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        createSmallDescription(info, width, height, false);
    }
    public void createSmallDescription(TooltipMakerAPI info, float width, float height,
                                       boolean forMarketConditionTooltip) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        info.addImage(faction.getLogo(), width, 128, opad);


        if (isEnding()) {
            if (missionResult != null && missionResult.payment < 0) {
                info.addPara("由于与 " + faction.getDisplayNameWithArticle() +
                                " 的关系不再紧密，你与其的特别舰队备案已中止。",
                        opad, faction.getBaseUIColor(),
                        faction.getDisplayNameWithArticleWithoutArticle());

                CoreReputationPlugin.addRequiredStanding(faction, TOffAcc_RelevantConstants.TOFFACC_LEAST_REP, null, null, info, tc, opad, true);
                CoreReputationPlugin.addCurrentStanding(faction, null, null, info, tc, opad);
            } else {
                info.addPara("你已撤销了与 " + faction.getDisplayNameWithArticle() +
                                " 的特别舰队备案。",
                        opad, faction.getBaseUIColor(),
                        faction.getDisplayNameWithArticleWithoutArticle());
            }
        } else {
            info.addPara("你获得了 %s 的特别舰队备案。",
                    opad, faction.getBaseUIColor(), Misc.ucFirst(faction.getPersonNamePrefix()));
        }

        if (!isEnding() && !isEnded()) {
            addAbandonButton(info, width, "撤销备案");
        }
    }

    public String getIcon() {
        return faction.getCrest();
    }

    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.remove(Tags.INTEL_ACCEPTED);
        tags.remove(Tags.INTEL_MISSIONS);
        tags.add(Tags.INTEL_AGREEMENTS);
        tags.add(faction.getId());
        return tags;
    }


    @Override
    protected MissionResult createAbandonedResult(boolean withPenalty) {
        return createResignedTOffAccResult(true, false, null);
    }

    public MissionResult createResignedTOffAccResult(boolean withPenalty, boolean inPerson, InteractionDialogAPI dialog) {
        if (withPenalty) {
            CustomRepImpact impact = new CustomRepImpact();
            impact.delta = -TOffAcc_RelevantConstants.TOFFACC_CANCEL_REP_DEC_RATIO * Global.getSettings().getFloat("factionCommissionResignPenalty");
            if (inPerson) {
                impact.delta = -TOffAcc_RelevantConstants.TOFFACC_CANCEL_REP_DEC_RATIO * Global.getSettings().getFloat("factionCommissionResignPenaltyInPerson");
            }
            ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                    new RepActionEnvelope(RepActions.CUSTOM,
                            impact, null, dialog != null ? dialog.getTextPanel() : null, false, true),
                    faction.getId());
            return new MissionResult();
        }
        return new MissionResult();
    }

    @Override
    protected MissionResult createTimeRanOutFailedResult() {
        return new MissionResult();
    }

    @Override
    protected String getMissionTypeNoun() {
        return "特别舰队备案";
    }

    @Override
    protected float getNoPenaltyAbandonDays() {
        return 0f;
    }



    public void reportEconomyMonthEnd() {
    }

    public void reportEconomyTick(int iterIndex) {

        float numIter = Global.getSettings().getFloat("economyIterPerMonth");
        float f = 1f / numIter;

        MonthlyReport report = SharedData.getData().getCurrentReport();

        FDNode fleetNode = report.getNode(MonthlyReport.FLEET);
        fleetNode.name = "Fleet";
        fleetNode.custom = MonthlyReport.FLEET;
        fleetNode.tooltipCreator = report.getMonthlyReportTooltip();

        float stipend = TOffAcc_utils.computeStipend();
        FDNode stipendNode = report.getNode(fleetNode, "node_id_TOffAcc_" + faction.getId());
        if (stipend>0)stipendNode.income += stipend * f;
        else if (stipend<0)stipendNode.upkeep +=stipend * f;

        if (stipendNode.name == null) {
            stipendNode.name = faction.getDisplayName() + " 特别舰队备案";
            stipendNode.icon = faction.getCrest();
            if (stipend>=0)stipendNode.tooltipCreator = new TooltipCreator() {
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }
                public float getTooltipWidth(Object tooltipParam) {
                    return 450;
                }
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addPara("作为 "+faction.getDisplayName() + " 特别舰队的每月行动资金", 0f);
                }
            };
            else stipendNode.tooltipCreator = new TooltipCreator() {
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }

                public float getTooltipWidth(Object tooltipParam) {
                    return 450;
                }

                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addPara("作为 " + faction.getDisplayName() + " 特别舰队的每月登记费用", 0f);
                }
            };
        }
    }
}






