public class Pokemon implements Battleable {
    private Species species;
    private int level;
    private IVs ivs;
    private int ev_hp, ev_hp_used;
    private int ev_atk, ev_atk_used;
    private int ev_def, ev_def_used;
    private int ev_spd, ev_spd_used;
    private int ev_spc, ev_spc_used;
    private int hp;
    private int atk;
    private int def;
    private int spd;
    private int spcAtk;
    private int spcDef;
    private int totalExp;
    private Moveset moves;
    private boolean wild;
    private boolean battleTower = false;
    private boolean atkBadge = false;
    private boolean defBadge = false;
    private boolean spdBadge = false;
    private boolean spcBadge = false;
    private boolean boostedExp = false;
    private boolean[] elementalBadgeBoosts = new boolean[17];

    // defaults to wild pokemon
    public Pokemon(Species s, int newLevel) {
        this(s, newLevel, true);
        setExpForLevel();
    }

    // default based off of species and level (works on all trainer pokemon,
    // except leaders' tms)
    public Pokemon(Species s, int newLevel, boolean wild) {
        species = s;
        level = newLevel;
        ivs = new IVs();
        setZeroEVs();
        moves = Moveset.defaultMoveset(species, level, Settings.isGS);
        calculateStats();
        this.wild = wild;
        setExpForLevel();
    }

    // will work for leaders
    public Pokemon(Species s, int newLevel, Moveset moves, boolean wild) {
        this(s, newLevel, wild);
        this.moves = moves;
        calculateStats();
        setExpForLevel();
    }

    public Pokemon(Species s, int newLevel, IVs ivs, boolean wild) {
        species = s;
        level = newLevel;
        this.ivs = ivs;
        setZeroEVs();
        moves = Moveset.defaultMoveset(species, level, Settings.isGS);
        calculateStats();
        this.wild = wild;
        setExpForLevel();
    }

    public Pokemon(Species s, int newLevel, Moveset moves, IVs ivs, boolean wild) {
        species = s;
        level = newLevel;
        this.ivs = ivs;
        setZeroEVs();
        this.moves = moves;
        calculateStats();
        this.wild = wild;
        setExpForLevel();
    }

    // Battle Tower poke
    public Pokemon(Species s, int newLevel, Moveset moves, IVs ivs, int hp, int atk, int def, int spe, int spA, int spD) {
        species = s;
        level = newLevel;
        this.ivs = ivs;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.spcAtk = spA;
        this.spcDef = spD;
        this.spd = spe;
        this.moves = moves;
        this.battleTower = true;
        setExpForLevel();
    }

    // TODO constructor which accepts EVs
    public void setZeroEVs() {
        ev_hp = ev_hp_used = 0;
        ev_atk = ev_atk_used = 0;
        ev_def = ev_def_used = 0;
        ev_spc = ev_spc_used = 0;
        ev_spd = ev_spd_used = 0;
    }

    // call this to update your stats
    // automatically called on level ups/rare candies, but not just from gaining
    // stat EV
    public void calculateStats() {
        ev_hp_used = ev_hp;
        ev_atk_used = ev_atk;
        ev_def_used = ev_def;
        ev_spc_used = ev_spc;
        ev_spd_used = ev_spd;
        hp = calcHPWithIV(ivs.getHPIV());
        atk = calcAtkWithIV(ivs.getAtkIV());
        def = calcDefWithIV(ivs.getDefIV());
        spd = calcSpdWithIV(ivs.getSpdIV());
        spcAtk = calcSpcAtkWithIV(ivs.getSpcIV());
        spcDef = calcSpcDefWithIV(ivs.getSpcIV());
    }

    private int calcHPWithIV(int iv) {
        return calcStatNumerator(iv, species.getBaseHP(), ev_hp_used) * level / 100
                + level + 10;
    }

    private int calcAtkWithIV(int iv) {
        return calcStatNumerator(iv, species.getBaseAtk(), ev_atk_used) * level
                / 100 + 5;
    }

    private int calcDefWithIV(int iv) {
        return calcStatNumerator(iv, species.getBaseDef(), ev_def_used) * level
                / 100 + 5;
    }

    private int calcSpdWithIV(int iv) {
        return calcStatNumerator(iv, species.getBaseSpd(), ev_spd_used) * level
                / 100 + 5;
    }

    private int calcSpcAtkWithIV(int iv) {
        return calcStatNumerator(iv, species.getBaseSpcAtk(), ev_spc_used) * level
                / 100 + 5;
    }

    private int calcSpcDefWithIV(int iv) {
        return calcStatNumerator(iv, species.getBaseSpcDef(), ev_spc_used) * level
                / 100 + 5;
    }

    private int evCalc(int ev) {
        return (Math.min((int) Math.ceil(Math.sqrt(ev)), 255)) / 4;
    }

    private int calcStatNumerator(int iv, int base, int ev) {
        return 2 * (iv + base) + evCalc(ev);
    }

    private void setExpForLevel() {
        totalExp = ExpCurve.lowestExpForLevel(species.getExpCurve(), level);
    }

    public IVs getIVs() {
        return ivs;
    }
    // TODO: EV setter

    public int getHP() {
        return hp;
    }

    // badge boosts
    public int getAtk() {
        return (int) (atkBadge ? 9 * atk / 8 : atk);
    }

    public int getDef() {
        return (int) (defBadge ? 9 * def / 8 : def);
    }

    public int getSpcAtk() {
        return (int) (spcBadge ? 9 * spcAtk / 8 : spcAtk);
    }

    public int getSpcDef() {
        int spatk_for_bug = getSpcAtk();
        if (spcBadge && Constants.givesSpDefBadgeBoost(spatk_for_bug)) {
            return 9 * spcDef / 8;
        }
        return spcDef;
    }

    public int getSpd() {
        return (int) (spdBadge ? 9 * spd / 8 : spd);
    }

    public int getSpdWithIV(int iv) {
        return calcSpdWithIV(iv);
    }

    // not affected by badge boosts
    public int getTrueAtk() {
        return atk;
    }

    public int getTrueDef() {
        return def;
    }

    public int getTrueSpcAtk() {
        return spcAtk;
    }

    public int getTrueSpcDef() {
        return spcDef;
    }

    public int getTrueSpd() {
        return spd;
    }

    public int getLevel() {
        return level;
    }

    public Species getSpecies() {
        return species;
    }

    public void setMoveset(Moveset m) {
        moves = m;
    }

    public Moveset getMoveset() {
        return moves;
    }

    public boolean isWild() {
        return wild;
    }

    public void setWild(boolean isWild) {
        this.wild = isWild;
    }

    public boolean isTowerPoke() {
        return battleTower;
    }

    public int getTotalExp() {
        return totalExp;
    }

    public int expGiven(int participants) {
        return (species.getKillExp() / participants) * level / 7 * 3
                / (isWild() ? 3 : 2);
    }

    public String toString() {
        return statsWithBoost();
    }

    public String statsWithBoost() {
        String endl = Constants.endl;
        StringBuilder sb = new StringBuilder();
        sb.append(levelName() + " ");
        sb.append("EXP Needed: " + expToNextLevel() + "/" + expForLevel()
                + endl);
        sb.append("Stats WITH badge boosts:" + endl);
        sb.append(String.format("  %1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", "HP",
                atkBadge ? "*ATK" : "ATK", defBadge ? "*DEF" : "DEF",
                spdBadge ? "*SPD" : "SPD", spcBadge ? "*SPCATK" : "SPCATK",
                spcBadge ? "*SPCDEF" : "SPCDEF")
                + endl);
        sb.append(String.format("  %1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", getHP(),
                getAtk(), getDef(), getSpd(), getSpcAtk(), getSpcDef()) + endl);
        sb.append(String.format("IV%1$7s%2$7s%3$7s%4$7s%5$7s%6$7s",
                ivs.getHPIV(), ivs.getAtkIV(), ivs.getDefIV(), ivs.getSpdIV(),
                ivs.getSpcIV(), ivs.getSpcIV()) + endl);
        sb.append(String.format("EV%1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", ev_hp,
                ev_atk, ev_def, ev_spd, ev_spc, ev_spc) + endl);
        sb.append(moves.toString() + endl);
        return sb.toString();
    }

    public String statsWithoutBoost() {
        String endl = Constants.endl;
        StringBuilder sb = new StringBuilder();
        sb.append(levelName() + " ");
        sb.append("EXP Needed: " + expToNextLevel() + "/" + expForLevel()
                + endl);
        sb.append("Stats WITHOUT badge boosts:" + endl);
        sb.append(String.format("  %1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", "HP",
                "ATK", "DEF", "SPD", "SPCATK", "SPCDEF") + endl);
        sb.append(String.format("  %1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", getHP(),
                getTrueAtk(), getTrueDef(), getTrueSpd(), getTrueSpcAtk(),
                getTrueSpcDef())
                + endl);
        sb.append(String.format("IV%1$7s%2$7s%3$7s%4$7s%5$7s%6$7s",
                ivs.getHPIV(), ivs.getAtkIV(), ivs.getDefIV(), ivs.getSpdIV(),
                ivs.getSpcIV(), ivs.getSpcIV()) + endl);
        sb.append(String.format("EV%1$7s%2$7s%3$7s%4$7s%5$7s%6$7s", ev_hp,
                ev_atk, ev_def, ev_spd, ev_spc, ev_spc) + endl);
        sb.append(moves.toString() + endl);
        return sb.toString();
    }

    // utility getters
    public String levelName() {
        return "L" + level + " " + getSpecies().getName();
    }

    public String pokeName() {
        return getSpecies().getName();
    }

    public String statsStr() {
        return String.format("%s/%s/%s/%s/%s/%s", getHP(), getAtk(), getDef(),
                getSpd(), getSpcAtk(), getSpcDef());
    }

    // experience methods
    // exp needed to get to next level
    public int expToNextLevel() {
        return ExpCurve.expToNextLevel(species.getExpCurve(), level, totalExp);
    }

    // total exp needed to get from this level to next level (no partial exp)
    public int expForLevel() {
        return ExpCurve.expForLevel(species.getExpCurve(), level);
    }

    // in game actions

    // gain num exp
    private void gainExp(int num) {
        num = (num * 3) / (boostedExp ? 2 : 3);
        num = (num * 3) / (luckyEgg ? 2 : 3);
        totalExp += num;
        // update lvl if necessary
        while (expToNextLevel() <= 0 && level < 100) {
            level++;
            calculateStats();
        }
    }

    // gain stat exp from a pokemon of species s
    private void gainStatExp(Species s, int participants) {
        ev_hp += (s.getBaseHP() / participants)  * (Settings.hasPokerus ? 2 : 1);
        ev_atk += (s.getBaseAtk() / participants)  * (Settings.hasPokerus ? 2 : 1);
        ev_def += (s.getBaseDef() / participants)  * (Settings.hasPokerus ? 2 : 1);
        ev_spc += (s.getBaseSpcAtk() / participants)  * (Settings.hasPokerus ? 2 : 1);
        ev_spd += (s.getBaseSpd() / participants) * (Settings.hasPokerus ? 2 : 1);
		
        ev_hp  = capEV(ev_hp);
        ev_atk = capEV(ev_atk);
        ev_def = capEV(ev_def);
        ev_spc = capEV(ev_spc);
        ev_spd = capEV(ev_spd);
    }

    private int capEV(int ev) {
        return Math.min(ev, 65535);
    }

    @Override
    public void battle(Pokemon p, BattleOptions options) {
        // p is the one that gets leveled up
        // this is the one that dies like noob
        // be sure to gain EVs before the exp
        p.gainStatExp(this.getSpecies(), options.getParticipants());
        p.gainExp(this.expGiven(options.getParticipants()));
    }

    // gains from eating stat/level boosters
    public void eatRareCandy() {
        if (level < 100) {
            level++;
            setExpForLevel();
            calculateStats();
        }
    }

    public void eatHPUp() {
        if (ev_hp >= 25600)
            return;
        ev_hp += 2560;
        calculateStats();
    }

    public void eatProtein() {
        if (ev_atk >= 25600)
            return;
        ev_atk += 2560;
        calculateStats();
    }

    public void eatIron() {
        if (ev_def >= 25600)
            return;
        ev_def += 2560;
        calculateStats();
    }

    public void eatCalcium() {
        if (ev_spc >= 25600)
            return;
        ev_spc += 2560;
        calculateStats();
    }

    public void eatCarbos() {
        if (ev_spd >= 25600)
            return;
        ev_spd += 2560;
        calculateStats();
    }

    // TODO: proper evolution
    public void evolve(Species s) {
        species = s;
    }

    // badge get/set
    public boolean isAtkBadge() {
        return atkBadge;
    }

    public void setAtkBadge(boolean atkBadge) {
        this.atkBadge = atkBadge;
    }

    public boolean isDefBadge() {
        return defBadge;
    }

    public void setDefBadge(boolean defBadge) {
        this.defBadge = defBadge;
    }

    public boolean isSpdBadge() {
        return spdBadge;
    }

    public void setSpdBadge(boolean spdBadge) {
        this.spdBadge = spdBadge;
    }

    public boolean isSpcBadge() {
        return spcBadge;
    }

    public void setSpcBadge(boolean spcBadge) {
        this.spcBadge = spcBadge;
    }

    public boolean isTypeBoosted(Type t) {
        int index = Type.typeIndex(t);
        if (index < 0 || index > 16) {
            return false;
        }
        return elementalBadgeBoosts[index];
    }

    public void setTypeBoosted(Type t) {
        int index = Type.typeIndex(t);
        if (index < 0 || index > 16) {
            return;
        }
        elementalBadgeBoosts[index] = true;
    }

    public void unboostType(Type t) {
        int index = Type.typeIndex(t);
        if (index < 0 || index > 16) {
            return;
        }
        elementalBadgeBoosts[index] = false;
    }

    public void setAllBadges() {
        atkBadge = true;
        defBadge = true;
        spdBadge = true;
        spcBadge = true;
    }

    public void loseAllBadges() {
        atkBadge = false;
        defBadge = false;
        spdBadge = false;
        spcBadge = false;
    }

    // a printout of stat ranges given this pokemon's EVs (not IVs)
    public String statRanges(boolean isBoosted) {
        int[] possibleHPs = new int[16];
        int[] possibleAtks = new int[16];
        int[] possibleDefs = new int[16];
        int[] possibleSpds = new int[16];
        int[] possibleSpcAtks = new int[16];
        int[] possibleSpcDefs = new int[16];
        if (isBoosted) {
            for (int i = 0; i < 16; i++) {
                possibleHPs[i] = calcHPWithIV(i);
                possibleAtks[i] = 9 * calcAtkWithIV(i) / 8;
                possibleDefs[i] = 9 * calcDefWithIV(i) / 8;
                possibleSpds[i] = 9 * calcSpdWithIV(i) / 8;
                possibleSpcAtks[i] = 9 * calcSpcAtkWithIV(i) / 8;
                possibleSpcDefs[i] = 9 * calcSpcDefWithIV(i) / 8;
            }
        } else {
            for (int i = 0; i < 16; i++) {
                possibleHPs[i] = calcHPWithIV(i);
                possibleAtks[i] = calcAtkWithIV(i);
                possibleDefs[i] = calcDefWithIV(i);
                possibleSpds[i] = calcSpdWithIV(i);
                possibleSpcAtks[i] = calcSpcAtkWithIV(i);
                possibleSpcDefs[i] = calcSpcDefWithIV(i);
            }
        }
        StringBuilder sb = new StringBuilder(levelName() + Constants.endl);
        sb.append("Stat ranges " + (isBoosted ? "WITH" : "WITHOUT")
                + " badge boosts:" + Constants.endl);
        sb.append("IV    |0   |1   |2   |3   |4   |5   |6   |7   |8   |9   |10  |11  |12  |13  |14  |15  "
                + Constants.endl
                + "------------------------------------------------------------------------------------"
                + Constants.endl);
        sb.append("HP    ");
        for (int i = 0; i < 16; i++) {
            sb.append(String.format("|%1$4s", possibleHPs[i])); // pad left,
                                                                // length 4
        }
        sb.append(Constants.endl);
        sb.append("ATK   ");
        for (int i = 0; i < 16; i++) {
            sb.append(String.format("|%1$4s", possibleAtks[i])); // pad left,
                                                                    // length 4
        }
        sb.append(Constants.endl);
        sb.append("DEF   ");
        for (int i = 0; i < 16; i++) {
            sb.append(String.format("|%1$4s", possibleDefs[i])); // pad left,
                                                                    // length 4
        }
        sb.append(Constants.endl);
        sb.append("SPD   ");
        for (int i = 0; i < 16; i++) {
            sb.append(String.format("|%1$4s", possibleSpds[i])); // pad left,
                                                                    // length 4
        }
        sb.append(Constants.endl);
        sb.append("SPATK ");
        for (int i = 0; i < 16; i++) {
            sb.append(String.format("|%1$4s", possibleSpcAtks[i])); // pad left,
                                                                    // length 4
        }
        sb.append(Constants.endl);
        sb.append("SPDEF ");
        for (int i = 0; i < 16; i++) {
            sb.append(String.format("|%1$4s", possibleSpcDefs[i])); // pad left,
                                                                    // length 4
        }
        sb.append(Constants.endl);

        return sb.toString();
    }
}
