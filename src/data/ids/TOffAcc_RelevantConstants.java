package data.ids;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import lunalib.lunaSettings.LunaSettings;

public class TOffAcc_RelevantConstants
{
    public static final String TOFFACC_MODID="transponder_off_accepted";

    public static final String TOFF_GRANTED="$TOffAcc_factionAllowPlayerTOff";
//    public static final String TOFF_FAC_NEED_TURN="$TOffAcc_factionNeedTurn";
    public static final String PATROL_LISTENER_MAP_KEY="$TOffAcc_PatrolSpawnListener";

    @Deprecated
    public static final String TOFFACC_IS_PREVIOUSLY_ENABLED="$ToffAcc_ModExist";

    public static final String TOFFACC_IS_PREVIOUSLY_ENABLED_1="$ToffAcc_ModExist_1";
    public static final String TOFFACC_NOT_SUITABLE="$TOffAcc_Unavailable";
    public static final String TOFFACC_PERSON_FORCE_PROVIDE="$TOffAcc_Force_Provide";

    public static RepLevel TOFFACC_LEAST_REP = getRep(false);
    public static RepLevel TOFFACC_LEAST_REVOKE_REP = getRep(true);

    public static float TOFFACC_STIPEND_RATIO = getTOffAccStipendRatio();
    public static float TOFFACC_CANCEL_REP_DEC_RATIO = getTOffAccCancelRepDecRatio();

    public static void reload()
    {
        TOFFACC_LEAST_REP = getRep(false);
        TOFFACC_LEAST_REVOKE_REP = getRep(true);

        TOFFACC_STIPEND_RATIO = getTOffAccStipendRatio();
        TOFFACC_CANCEL_REP_DEC_RATIO = getTOffAccCancelRepDecRatio();
    }

    private static float getTOffAccStipendRatio()
    {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            return LunaSettings.getFloat(TOFFACC_MODID,"TOffAccStipendRatio");
        }
        else return Global.getSettings().getFloat("TOffAccStipendRatio");
    }

    private static float getTOffAccCancelRepDecRatio()
    {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            return LunaSettings.getFloat(TOFFACC_MODID,"TOffAccCancelReputationDecreaseRatio");
        }
        else return Global.getSettings().getFloat("TOffAccCancelReputationDecreaseRatio");
    }

    private static RepLevel getRep(boolean revoke)
    {
        String repString;
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            if (revoke)repString=LunaSettings.getString(TOFFACC_MODID,"TOffAccLeastRevokeReputation");
            else repString=LunaSettings.getString(TOFFACC_MODID,"TOffAccLeastReputation");
        }
        else
        {
            if (revoke)repString=Global.getSettings().getString("TOffAccLeastRevokeReputation");
            else repString=Global.getSettings().getString("TOffAccLeastReputation");
        }
        switch (repString)
        {
            case "vengeful":return RepLevel.VENGEFUL;
            case "hostile":return RepLevel.HOSTILE;
            case "inhospitable":return RepLevel.INHOSPITABLE;
            case "suspicious":return RepLevel.SUSPICIOUS;
            case "neutral":return RepLevel.NEUTRAL;
            case "favorable":return RepLevel.FAVORABLE;
            case "welcoming":return RepLevel.WELCOMING;
            case "friendly":return RepLevel.FRIENDLY;
            case "cooperative":return RepLevel.COOPERATIVE;
            default:return null;
        }
    }
}
