import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.ini4j.jdk14.edu.emory.mathcs.backport.java.util.Arrays;

public class RouteParser {
    public static int lineNum = 0;

    public static List<GameAction> parseFile(String fileName) {
        lineNum = 0;
        ArrayList<GameAction> actions = new ArrayList<GameAction>();

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fileName)));
            while (in.ready()) {
                lineNum++;
                String wholeLine = in.readLine();
                String[] lines = wholeLine.split("//"); // remove comments
                String line = lines[0];
                GameAction a = null;
                try {
                    a = parseLine(line);
                } catch (Exception e) {
                    Main.appendln("Error in line " + lineNum + ": (no further information)");
                }
                if (a != null)
                    actions.add(a);

            }
            in.close();
        } catch (FileNotFoundException e) {
            Main.appendln("Could not find Route file: `" + fileName + "`");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return actions;
    }

    // assumes no comments
    private static GameAction parseLine(String line) throws Exception {
        String[] tokens = line.split(" ");
        int n = tokens.length;
        if (n == 0)
            return null;

        String firstToken = tokens[0];
        // trainer offset
        if (firstToken.startsWith("0x") || firstToken.startsWith("0X")) {
            Battleable b = Trainer.getTrainer(Integer.parseInt(
                    firstToken.substring(2), 16));
            if (b == null) {
                Main.appendln("ERROR ON LINE "
                        + lineNum
                        + ": that offset doesn't exist. Are you sure you're setting the right game?");
                return null;
            }
            String[] flagTokens = (String[]) Arrays.copyOfRange(tokens, 1, n);
            return addFlagsToBattleable(b, flagTokens);
        }
        // L(num), to indicate pokemon
        else if (firstToken.matches("[Ll][0-9]+")) {
            if(n < 2) {
                Main.appendln("ERROR ON LINE " + lineNum + ": wrong wild pokémon.");
                return null;
            }
            int lvl = Integer.parseInt(firstToken.substring(1));
            String species = tokens[1];
            IVs ivs = null;
            try
            {
                Integer atk = Integer.parseInt(tokens[2]);
                Integer def = Integer.parseInt(tokens[3]);
                Integer spd = Integer.parseInt(tokens[4]);
                Integer spc = Integer.parseInt(tokens[5]);
                ivs = new IVs(atk,def,spd,spc);
            }
            catch(Exception exc)
            {
                ivs = new IVs(15,15,15,15);
            }
            Pokemon b = new Pokemon(Species.valueOf(species),lvl,ivs,true); //default to wild pokemon
            if (b.getSpecies() == null) {
                Main.appendln("ERROR ON LINE " + lineNum + ": bad pokemon name");
                return null;
            }
            
            String[] flagTokens = (String[]) Arrays.copyOfRange(tokens, 2, n);
            return addFlagsToBattleable(b, flagTokens);		}
        // evolve
        else if (firstToken.equalsIgnoreCase("e")
                || firstToken.equalsIgnoreCase("evolve")) {
            if (n < 2) {
                Main.appendln("ERROR ON LINE " + lineNum + ": missing evolve arguments.");
                return null;
            }
            String species = tokens[1];
            return new Evolve(species);
        }
        // learnmove
        else if (firstToken.equalsIgnoreCase("lm")
                || firstToken.equalsIgnoreCase("learnmove")) {
            if (n < 2) {
                Main.appendln("ERROR ON LINE " + lineNum + ": missing learnmove arguments.");
                return null;
            }
            String move = tokens[1];
            LearnMove l = new LearnMove(move);
            if (l.getMove() == null) {
                Main.appendln("ERROR ON LINE " + lineNum + ": bad move name");
                return null;
            }
            return l;
        }
        // unlearnmove
        else if (firstToken.equalsIgnoreCase("um")
                || firstToken.equalsIgnoreCase("unlearnmove")) {
            if (n < 2) {
                Main.appendln("ERROR ON LINE " + lineNum + ": missing unlearnmove arguments.");
                return null;
            }
            String move = tokens[1];
            UnlearnMove l = new UnlearnMove(move);
            if (l.getMove() == null) {
                Main.appendln("ERROR ON LINE " + lineNum + ": bad move name");
                return null;
            }
            return l;
        }
        
        else if (firstToken.equalsIgnoreCase("addMoney")) {
        	if (n < 2) {
                Main.appendln("ERROR ON LINE " + lineNum + ": money missing");
                return null;
            }
            try{
            	int money = Integer.parseInt(tokens[1]);
            	return new AddMoney(money);
            } catch (Exception e) {
            	Main.appendln("ERROR ON LINE " + lineNum + ": money needs to be an integer");
                return null;
            }
        }
        
        else if (firstToken.equalsIgnoreCase("spendMoney")) {
        	if (n < 2) {
                Main.appendln("ERROR ON LINE " + lineNum + ": money missing");
                return null;
            }
            try{
            	int money = Integer.parseInt(tokens[1]);
            	return new AddMoney(-money);
            } catch (Exception e) {
            	Main.appendln("ERROR ON LINE " + lineNum + ": money needs to be an integer");
                return null;
            }
        }
        
        else if (firstToken.equalsIgnoreCase("buy")) {
        	if (n < 2) {
                Main.appendln("ERROR ON LINE " + lineNum + ": item missing");
                return null;
            }
        	try {
        		String itemName = tokens[1];
        		Item item = Item.getItemByName(itemName);
        		int quantity = 1;
        		if (n > 2) {
        			quantity = Integer.parseInt(tokens[2]);
        			if (quantity < 1)
        				throw new NumberFormatException();
        		}
        		
        		int cost = item.getBuyPrice()*quantity;
        		return new AddMoney(-cost);
        	} catch(NumberFormatException nfe) {
        		Main.appendln("ERROR ON LINE " + lineNum + ": quantity must be > 1");
        	} catch (Exception e) {
        		Main.appendln("ERROR ON LINE " + lineNum + ": unknown item name");
                return null;
        	}
        }
        
        else if (firstToken.equalsIgnoreCase("sell")) {
        	if (n < 2) {
                Main.appendln("ERROR ON LINE " + lineNum + ": item missing");
                return null;
            }
        	try {
        		String itemName = tokens[1];
        		Item item = Item.getItemByName(itemName);
        		int quantity = 1;
        		if (n > 2) {
        			quantity = Integer.parseInt(tokens[2]);
        			if (quantity < 1)
        				throw new NumberFormatException();
        		}
        		
        		int earn = item.getSellPrice()*quantity;
        		return new AddMoney(earn);
        	} catch(NumberFormatException nfe) {
        		Main.appendln("ERROR ON LINE " + lineNum + ": quantity must be > 1");
        	} catch (Exception e) {
        		Main.appendln("ERROR ON LINE " + lineNum + ": unknown item name");
                return null;
        	}
        }
        
        // candies, etc
        else if (firstToken.equalsIgnoreCase("rc")
                || firstToken.equalsIgnoreCase("rarecandy")) {
            return GameAction.eatRareCandy;
        } else if (firstToken.equalsIgnoreCase("hpup")) {
            return GameAction.eatHPUp;
        } else if (firstToken.equalsIgnoreCase("iron")) {
            return GameAction.eatIron;
        } else if (firstToken.equalsIgnoreCase("protein")) {
            return GameAction.eatProtein;
        } else if (firstToken.equalsIgnoreCase("calcium")) {
            return GameAction.eatCalcium;
        } else if (firstToken.equalsIgnoreCase("carbos")) {
            return GameAction.eatCarbos;
        }

        else if(firstToken.equalsIgnoreCase("zephyrbadge")) {
            return GameAction.getZephyrBadge;
        }
        else if(firstToken.equalsIgnoreCase("hivebadge")) {
            return GameAction.getHiveBadge;
        }
        else if(firstToken.equalsIgnoreCase("plainbadge")) {
            return GameAction.getPlainBadge;
        }
        else if(firstToken.equalsIgnoreCase("fogbadge")) {
            return GameAction.getFogBadge;
        }
        else if(firstToken.equalsIgnoreCase("stormbadge")) {
            return GameAction.getStormBadge;
        }		
        else if(firstToken.equalsIgnoreCase("mineralbadge")) {
            return GameAction.getMineralBadge;
        }
        else if(firstToken.equalsIgnoreCase("glacierbadge")) {
            return GameAction.getGlacierBadge;
        }
        else if(firstToken.equalsIgnoreCase("risingbadge")) {
            return GameAction.getRisingBadge;
        }		
        else if(firstToken.equalsIgnoreCase("boulderbadge")) {
            return GameAction.getBoulderBadge;
        }		
        else if(firstToken.equalsIgnoreCase("cascadebadge")) {
            return GameAction.getCascadeBadge;
        }		
        else if(firstToken.equalsIgnoreCase("thunderbadge")) {
            return GameAction.getThunderBadge;
        }		
        else if(firstToken.equalsIgnoreCase("rainbowbadge")) {
            return GameAction.getRainbowBadge;
        }		
        else if(firstToken.equalsIgnoreCase("soulbadge")) {
            return GameAction.getSoulBadge;
        }		
        else if(firstToken.equalsIgnoreCase("marshbadge")) {
            return GameAction.getMarshBadge;
        }		
        else if(firstToken.equalsIgnoreCase("volcanobadge")) {
            return GameAction.getVolcanoBadge;
        }		
        else if(firstToken.equalsIgnoreCase("earthbadge")) {
            return GameAction.getEarthBadge;
        }		
        
        else if(firstToken.equalsIgnoreCase("battletowerflag")) {
            Constants.battleTower = true;
            return GameAction.battleTowerFlag;
        }
        else if(firstToken.equalsIgnoreCase("returnpower")) {
            int newPower = Integer.parseInt(tokens[1]);
            return new ChangeReturnPower(newPower);
        }		
        else if (firstToken.equalsIgnoreCase("blackBelt")) {
            return GameAction.equipBlackBelt;
        }
        else if (firstToken.equalsIgnoreCase("blackGlasses")) {
            return GameAction.equipBlackGlasses;
        }
        else if (firstToken.equalsIgnoreCase("charcoal")) {
            return GameAction.equipCharcoal;
        }
        else if (firstToken.equalsIgnoreCase("dragonScale")) {
            return GameAction.equipDragonScale;
        }
        else if (firstToken.equalsIgnoreCase("hardStone")) {
            return GameAction.equipHardStone;
        }
        else if (firstToken.equalsIgnoreCase("magnet")) {
            return GameAction.equipMagnet;
        }
        else if (firstToken.equalsIgnoreCase("metalcoat")) {
            return GameAction.equipMetalCoat;
        }
        else if (firstToken.equalsIgnoreCase("miracleSeed")) {
            return GameAction.equipMiracleSeed;
        }
        else if (firstToken.equalsIgnoreCase("mysticWater")) {
            return GameAction.equipMysticWater;
        }
        else if (firstToken.equalsIgnoreCase("neverMeltIce")) {
            return GameAction.equipNeverMeltIce;
        }
        else if (firstToken.equalsIgnoreCase("pinkBow")) {
            return GameAction.equipPinkBow;
        }
        else if (firstToken.equalsIgnoreCase("poisonBarb")) {
            return GameAction.equipPoisonBarb;
        }
        else if (firstToken.equalsIgnoreCase("polkadotBow")) {
            return GameAction.equipPolkadotBow;
        }
        else if (firstToken.equalsIgnoreCase("sharpBeak")) {
            return GameAction.equipSharpBeak;
        }
        else if (firstToken.equalsIgnoreCase("silverPowder")) {
            return GameAction.equipSilverPowder;
        }
        else if (firstToken.equalsIgnoreCase("softSand")) {
            return GameAction.equipSoftSand;
        }
        else if (firstToken.equalsIgnoreCase("spellTag")) {
            return GameAction.equipSpellTag;
        }
        else if (firstToken.equalsIgnoreCase("twistedSpoon")) {
            return GameAction.equipTwisterSpoon;
        }
        else if (firstToken.equalsIgnoreCase("lightBall")) {
            return GameAction.equipLightBall;
        }
        else if (firstToken.equalsIgnoreCase("metalPowder")) {
            return GameAction.equipMetalPowder;
        }
        else if (firstToken.equalsIgnoreCase("thickClub")) {
            return GameAction.equipThickClub;
        }
        else if (firstToken.equalsIgnoreCase("luckyEgg")) {
            return GameAction.equipLuckyEgg;
        }
        else if (firstToken.equalsIgnoreCase("unequip")) {
            return GameAction.unequip;
        }
        
        else if(firstToken.equalsIgnoreCase("setAmuletCoin")) {
        	return GameAction.setAmuletCoin;
        }        
        else if(firstToken.equalsIgnoreCase("unsetAmuletCoin")) {
        	return GameAction.unsetAmuletCoin;
        }

        else if (firstToken.equalsIgnoreCase("pokerus")) {
            return GameAction.infectPokerus;
        }

        else if (firstToken.equalsIgnoreCase("setBoostedExp")) {
            return GameAction.setBoostedExp;
        }

        else if (firstToken.equalsIgnoreCase("unsetBoostedExp")) {
            return GameAction.unsetBoostedExp;
        }


        // printing commands        
        else if(firstToken.equalsIgnoreCase("money")) {
        	return GameAction.printMoney;
        }
        else if (firstToken.equalsIgnoreCase("stats")) {
            if (n == 1) {
                return GameAction.printAllStatsNoBoost;
            } else if (tokens[1].equalsIgnoreCase("-b")) {
                return GameAction.printAllStats;
            } else {
                return GameAction.printAllStatsNoBoost;
            }
        } else if (firstToken.equalsIgnoreCase("ranges")) {
            if (n == 1) {
                return GameAction.printStatRangesNoBoost;
            } else if (tokens[1].equalsIgnoreCase("-b")) {
                return GameAction.printStatRanges;
            } else {
                return GameAction.printStatRangesNoBoost;
            }
        } else if (!firstToken.trim().isEmpty()) {
            Battleable b = null;
            // attempt to parse as trainer name
            if(!Constants.battleTower) {
                b = Trainer.getTrainer(firstToken.toUpperCase());
            } else {
                b = Trainer.getTowerPoke(firstToken.toUpperCase());
            }
            if (b == null) {
                Main.appendln("ERROR ON LINE "
                        + lineNum
                        + ": that trainer doesn't exist. Check for typos, and make sure you use offsets for rockets (their names repeat)");
                return null;
            }
            String[] flagTokens = (String[]) Arrays.copyOfRange(tokens, 1, n);
            return addFlagsToBattleable(b, flagTokens);
        }
        return null;
    }

    enum NextFlag {
        ANY_FLAG, XITEMS, YITEMS, 
        XATK, YATK, XDEF, YDEF, XSPD, YSPD, XSPC, YSPC, XSPDEF, YSPDEF, XACC, 
        VERBOSE, SXP, SXPS, 
        XATKS, XDEFS, XSPDS, XSPCS, XSPDEFS, YDEFS, YSPDEFS, 
        ORDER, BBS,
        XREFLECT, YREFLECT, XLIGHTSCREEN, YLIGHTSCREEN,
        WEATHER
    }

    private static GameAction addFlagsToBattleable(Battleable b,
            String[] flagTokens) throws Exception {
        NextFlag nf = NextFlag.ANY_FLAG;
        BattleOptions options = new BattleOptions();

        for (String s : flagTokens) {
            // we're looking for a flag
            if (nf == NextFlag.ANY_FLAG) {
                // set this pokemon to wild
                if (s.equalsIgnoreCase("-w") || s.equalsIgnoreCase("-wild")) {
                    if (b instanceof Trainer) {
                        Main.appendln("ERROR ON LINE " + lineNum +": can't use -w or -wild on trainers.");
                        return null;
                        // can't use -wild or -trainer flag on trainers
                    }
                    ((Pokemon) b).setWild(true);
                    nf = NextFlag.ANY_FLAG;
                    continue;
                }
                // set this pokemon to trainer
                else if (s.equalsIgnoreCase("-t")
                        || s.equalsIgnoreCase("-trainer")) {
                    if (b instanceof Trainer) {
                        Main.appendln("ERROR ON LINE " + lineNum +": can't use -t or -trainer on trainers.");
                        return null;
                        // can't use -wild or -trainer flag on trainers
                    }
                    ((Pokemon) b).setWild(false);
                    nf = NextFlag.ANY_FLAG;
                    continue;
                }
                // xitems (sm1)
                else if (s.equalsIgnoreCase("-x")
                        || s.equalsIgnoreCase("-xitems")) {
                    nf = NextFlag.XITEMS;
                    continue;
                }
                // yitems (sm2)
                else if (s.equalsIgnoreCase("-y")
                        || s.equalsIgnoreCase("-yitems")) {
                    nf = NextFlag.YITEMS;
                    continue;
                }
                // all the x items (and y items)
                else if (s.equalsIgnoreCase("-xatk")) {
                    nf = NextFlag.XATK;
                    continue;
                } else if (s.equalsIgnoreCase("-yatk")) {
                    nf = NextFlag.YATK;
                    continue;
                } else if (s.equalsIgnoreCase("-xdef")) {
                    nf = NextFlag.XDEF;
                    continue;
                } else if (s.equalsIgnoreCase("-ydef")) {
                    nf = NextFlag.YDEF;
                    continue;
                } else if (s.equalsIgnoreCase("-xspd")) {
                    nf = NextFlag.XSPD;
                    continue;
                } else if (s.equalsIgnoreCase("-yspd")) {
                    nf = NextFlag.YSPD;
                    continue;
                } else if (s.equalsIgnoreCase("-xspc") || s.equalsIgnoreCase("-xspcatk")) {
                    nf = NextFlag.XSPC;
                    continue;
                } else if (s.equalsIgnoreCase("-yspc") || s.equalsIgnoreCase("-yspcatk")) {
                    nf = NextFlag.YSPC;
                    continue;
                } else if (s.equalsIgnoreCase("-xspcdef")){
                	nf = NextFlag.XSPDEF;
                	continue;
                } else if (s.equalsIgnoreCase("-yspcdef")){
                	nf = NextFlag.YSPDEF;
                	continue;
                }
                // xacc
                else if (s.equalsIgnoreCase("-xacc")) {
                    options.getMod1().useXAcc();
                    nf = NextFlag.ANY_FLAG;
                    continue;
                }
                // verbose
                else if (s.equalsIgnoreCase("-v")
                        || s.equalsIgnoreCase("-verbose")) {
                    nf = NextFlag.VERBOSE;
                    continue;
                }
                // split exp
                else if (s.equalsIgnoreCase("-sxp")) {
                    nf = NextFlag.SXP;
                    continue;
                }
                else if (s.equalsIgnoreCase("-sxps")) {
                    nf = NextFlag.SXPS;
                    continue;
                }
                else if (s.equalsIgnoreCase("-xatks")) {
                    nf = NextFlag.XATKS;
                    continue;
                }
                else if (s.equalsIgnoreCase("-xdefs")) {
                    nf = NextFlag.XDEFS;
                    continue;
                }
                else if (s.equalsIgnoreCase("-xspds")) {
                    nf = NextFlag.XSPDS;
                    continue;
                }
                else if (s.equalsIgnoreCase("-xspcs") || s.equalsIgnoreCase("-xspcatks")) {
                    nf = NextFlag.XSPCS;
                    continue;
                } else if (s.equalsIgnoreCase("-xspcdefs")){
                	nf = NextFlag.XSPDEFS;
                	continue;
                } else if (s.equalsIgnoreCase("-ydefs")) {
                    nf = NextFlag.YDEFS;
                    continue;
                } else if (s.equalsIgnoreCase("-yspcdefs")){
                	nf = NextFlag.YSPDEFS;
                	continue;
                } else if (s.equalsIgnoreCase("-order")) {
                    nf = NextFlag.ORDER;
                    continue;
                } else if (s.equalsIgnoreCase("-weather")) {
                	nf = NextFlag.WEATHER;
                    continue;
                }
                // print stat ranges if level
                else if (s.equalsIgnoreCase("-lvranges")) {
                    options.setPrintSRsOnLvl(true);
                    nf = NextFlag.ANY_FLAG;
                    continue;
                } else if (s.equalsIgnoreCase("-lvrangesb")) {
                    options.setPrintSRsBoostOnLvl(true);
                    nf = NextFlag.ANY_FLAG;
                    continue;
                }
                // badge boosts
                else if (s.equalsIgnoreCase("-bbs")) {
                    nf = NextFlag.BBS;
                    continue;
                }
                // screens
                else if (s.equalsIgnoreCase("-xreflect")) {
                    nf = NextFlag.XREFLECT;
                    continue;
                }
                else if (s.equalsIgnoreCase("-yreflect")) {
                    nf = NextFlag.YREFLECT;
                    continue;
                }
                else if (s.equalsIgnoreCase("-xlightscreen")) {
                    nf = NextFlag.XLIGHTSCREEN;
                    continue;
                }
                else if (s.equalsIgnoreCase("-ylightscreen")) {
                    nf = NextFlag.YLIGHTSCREEN;
                    continue;
                }
            }
            // -x flag
            else if (nf == NextFlag.XITEMS) {
                String[] nums = s.split("/");
                if (nums.length != 4 && nums.length != 5) {
                    Main.appendln("ERROR ON LINE " + lineNum +": -x or -xitems needs 4 arguments (5 if X Accuracy)");
                    return null;
                }
                options.getMod1().incrementAtkStage(Integer.parseInt(nums[0]));
                options.getMod1().incrementDefStage(Integer.parseInt(nums[1]));
                options.getMod1().incrementSpdStage(Integer.parseInt(nums[2]));
                options.getMod1().incrementSpcAtkStage(
                        Integer.parseInt(nums[3]));
                if (nums.length == 5) {
                    if (Integer.parseInt(nums[4]) != 0) {
                        options.getMod1().useXAcc();
                    }
                }
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            // -y flag
            else if (nf == NextFlag.YITEMS) {
                String[] nums = s.split("/");
                if (nums.length != 4 && nums.length != 5) {
                    Main.appendln("ERROR ON LINE " + lineNum +": -y or -yitems needs 4 arguments (5 if X Accuracy)");
                    return null;
                }
                options.getMod2().incrementAtkStage(Integer.parseInt(nums[0]));
                options.getMod2().incrementDefStage(Integer.parseInt(nums[1]));
                options.getMod2().incrementSpdStage(Integer.parseInt(nums[2]));
                options.getMod2().incrementSpcAtkStage(
                        Integer.parseInt(nums[3]));
                nf = NextFlag.ANY_FLAG;
                continue;
                // ignore y accuracy
            }
            // all xitem and yitem flags
            else if (nf == NextFlag.XATK) {
                options.getMod1().incrementAtkStage(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if (nf == NextFlag.YATK) {
                options.getMod2().incrementAtkStage(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if (nf == NextFlag.XDEF) {
                options.getMod1().incrementDefStage(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if (nf == NextFlag.YDEF) {
                options.getMod2().incrementDefStage(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if (nf == NextFlag.XSPD) {
                options.getMod1().incrementSpdStage(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if (nf == NextFlag.YSPD) {
                options.getMod2().incrementSpdStage(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if (nf == NextFlag.XSPC) {
                options.getMod1().incrementSpcAtkStage(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if (nf == NextFlag.YSPC) {
                options.getMod2().incrementSpcAtkStage(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if (nf == NextFlag.XSPDEF) {
            	options.getMod1().incrementSpcDefStage(Integer.parseInt(s));
            	nf = NextFlag.ANY_FLAG;
            	continue;
            } else if (nf == NextFlag.YSPDEF) {
            	options.getMod2().incrementSpcDefStage(Integer.parseInt(s));
            	nf = NextFlag.ANY_FLAG;
            	continue;
            }
            // verbose
            else if (nf == NextFlag.VERBOSE) {
                if (s.matches("[0-9]+")) {
                    options.setVerbose(Integer.parseInt(s));
                } else if (s.equalsIgnoreCase("NONE")) {
                    options.setVerbose(BattleOptions.NONE);
                } else if (s.equalsIgnoreCase("SOME")) {
                    options.setVerbose(BattleOptions.SOME);
                } else if (s.equalsIgnoreCase("ALL")) {
                    options.setVerbose(BattleOptions.ALL);
                } else if(s.equalsIgnoreCase("EVERYTHING")) {
                    options.setVerbose(BattleOptions.EVERYTHING);
                }
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            // sxp
            else if (nf == NextFlag.SXP) {
                options.setParticipants(Integer.parseInt(s));
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            else if(nf == NextFlag.SXPS) {
                String[] nums = s.split("/");
                Integer[] sxps = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    sxps[i] = Integer.parseInt(nums[i]);
                }
                options.setSxps(sxps);
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            else if(nf == NextFlag.XATKS) {
                String[] nums = s.split("/");
                Integer[] xatks = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    xatks[i] = Integer.parseInt(nums[i]);
                }
                options.setXatks(xatks);
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            else if(nf == NextFlag.XDEFS) {
                String[] nums = s.split("/");
                Integer[] xdefs = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    xdefs[i] = Integer.parseInt(nums[i]);
                }
                options.setXdefs(xdefs);
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            else if(nf == NextFlag.XSPDS) {
                String[] nums = s.split("/");
                Integer[] xspds = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    xspds[i] = Integer.parseInt(nums[i]);
                }
                options.setXspds(xspds);
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            else if(nf == NextFlag.XSPCS) {
                String[] nums = s.split("/");
                Integer[] xspcs = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    xspcs[i] = Integer.parseInt(nums[i]);
                }
                options.setXspcs(xspcs);
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if(nf == NextFlag.XSPDEFS) {
                String[] nums = s.split("/");
                Integer[] xspdefs = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    xspdefs[i] = Integer.parseInt(nums[i]);
                }
                options.setXspdefs(xspdefs);
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if(nf == NextFlag.YDEFS) {
                String[] nums = s.split("/");
                Integer[] ydefs = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    ydefs[i] = Integer.parseInt(nums[i]);
                }
                options.setYdefs(ydefs);
                nf = NextFlag.ANY_FLAG;
                continue;
            } else if(nf == NextFlag.YSPDEFS) {
                String[] nums = s.split("/");
                Integer[] yspdefs = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    yspdefs[i] = Integer.parseInt(nums[i]);
                }
                options.setYspdefs(yspdefs);
                nf = NextFlag.ANY_FLAG;
                continue;
            } 
            else if(nf == NextFlag.ORDER) {
                String[] nums = s.split("/");
                Integer[] order = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    order[i] = Integer.parseInt(nums[i]);
                }
                options.setOrder(order);
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            // badge boosts
            else if (nf == NextFlag.BBS) {
                String[] nums = s.split("/");
                if (nums.length != 4) {
                    Main.appendln("ERROR ON LINE " + lineNum +": -bbs needs 4 arguments");
                    return null;
                }
                int atkBB = Integer.parseInt(nums[0]);
                int defBB = Integer.parseInt(nums[1]);
                int spdBB = Integer.parseInt(nums[2]);
                int spcBB = Integer.parseInt(nums[3]);
                options.getMod1().setBadgeBoosts(atkBB, defBB, spdBB, spcBB);
                nf = NextFlag.ANY_FLAG;
                continue;
            }
            //screens
            else if (nf == NextFlag.XLIGHTSCREEN || nf == NextFlag.YLIGHTSCREEN) {
            	String[] nums = s.split("/");
                Integer[] lightscreen = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    lightscreen[i] = Integer.parseInt(nums[i]);
                }
                if (nf == NextFlag.XLIGHTSCREEN)
                	options.setXlightscreen(lightscreen);
                else if (nf == NextFlag.YLIGHTSCREEN)
                	options.setYlightscreen(lightscreen);
                
            	nf = NextFlag.ANY_FLAG;
            	continue;
            }
            else if (nf == NextFlag.XREFLECT || nf == NextFlag.YREFLECT) {
            	String[] nums = s.split("/");
                Integer[] reflect = new Integer[nums.length];
                for(int i=0; i<nums.length; i++) {
                    reflect[i] = Integer.parseInt(nums[i]);
                }
                if (nf == NextFlag.XREFLECT)
                	options.setXreflect(reflect);
                else if (nf == NextFlag.YREFLECT)
                	options.setYreflect(reflect);
                
            	nf = NextFlag.ANY_FLAG;
            	continue;
            } else if (nf == NextFlag.WEATHER) {
            	String[] weatherStrArr = s.split("/");
            	Weather w = Weather.NONE;
            	Weather[] weathers = Weather.getEmptyArray();
            	for (int i = 0; i < weatherStrArr.length; i++) {
	            	w = Weather.getWeatherFromName(weatherStrArr[i]);
	            	if(w == null) {
	            		Main.appendln("ERROR ON LINE " + lineNum + ": received `"+s+"`. -weather must be RAIN, SUN, SANDSTORM or NONE/0 (or a list of these).");
	            		nf = NextFlag.ANY_FLAG;
	            		continue;
	            	}
	            	weathers[i] = w;
            	}
            	if(weatherStrArr.length == 1)
            		options.setWeathers(w);
            	else
            		options.setWeathers(weathers);
            	
            	nf = NextFlag.ANY_FLAG;
            	continue;
            }
        }
        if (nf != NextFlag.ANY_FLAG) {
            // TODO: error check
        }
        return new Battle(b, options);
    }
}
