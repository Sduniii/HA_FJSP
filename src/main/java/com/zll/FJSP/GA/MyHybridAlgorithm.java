package com.zll.FJSP.GA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.zll.FJSP.Data.Job;
import com.zll.FJSP.Data.Problem;
import com.zll.FJSP.Data.Operation;
import com.zll.FJSP.Data.Solution;
import com.zll.FJSP.NeighbourSearch2.NeiborAl2;


/**
 * Description:遗传算法
 *
 * @author zll-hust E-mail:zh20010728@126.com
 * @date 创建时间：2020年5月28日 下午2:58:10
 */
public class MyHybridAlgorithm {
    private Problem input;
    private Operation[][] operationMatrix;
    private Random r;
    private Chromosome[] parents;
    private Chromosome[] children;
    private Job[] jobs;
    private ChromosomeOperation chromOps;
    private Chromosome currentBest;
    private Chromosome best;
    private int noImprove = 0;
    private int gen = 0;
    private CaculateFitness c;
    private Solution bestSolution;

    private final int popSize = 400;// population size 400
    private double pr = 0.005;// Reproduction probability
    //private final double m;
    private double pc = 0.80;// Crossover probability
    private double pm = 0.10;// Mutation probability

    private final int maxT = 9;// tabu list length
    private final int maxTabuLimit = 100;// maxTSIterSize = maxTabuLimit * (Gen / maxGen)
    private final double pt = 0.05;// tabu probability

    private final double pp = 0.30;// perturbation probability

    private int maxGen = 100;// iterator for 200 time for each loop
    private final int maxStagnantStep = 20;// max iterator no improve
    private final int timeLimit = -1;// no time limit

    public MyHybridAlgorithm(Problem input, int generations) {
        maxGen = generations;
        //this.m = (1 - pr) / (maxGen);
        this.input = input;
        this.operationMatrix = new Operation[input.getJobCount()][];

        for (int i = 0; i < operationMatrix.length; i++) {
            operationMatrix[i] = new Operation[input.getOperationCountArr()[i]];
            for (int j = 0; j < operationMatrix[i].length; j++)
                operationMatrix[i][j] = new Operation();
        }

        this.r = new Random();
//		this.r.setSeed(1);
    }

    /**
     * the whole logic of the flexible job shop sheduling problem
     */
    public void solve() {
        int jobCount = input.getJobCount();
        c = new CaculateFitness();
        chromOps = new ChromosomeOperation(r, input);
//        TabuSearch1 tabu = new TabuSearch1(input, r);

        // 初始化工件类entries
        int[][] operationToIndex = input.getOperationToIndex();
        jobs = new Job[jobCount];
        for (int i = 0; i < jobCount; i++) {
            int index = i;// 工件编号
            int opsNr = input.getOperationCountArr()[i];// 工件工序数
            int[] opsIndex = operationToIndex[i];// 工件工序对应的index
            int[] opsMacNr = new int[opsNr];// 工序对应备选机器数
            for (int j = 0; j < opsNr; j++) {
                opsMacNr[j] = input.getMachineCountArr()[opsIndex[j]];
            }
            jobs[i] = new Job(index, opsNr, opsIndex, opsMacNr);
        }

        // 随机生成初始种群
        parents = new Chromosome[this.popSize];// 染色体
        for (int i = 0; i < this.popSize; i++) {
            parents[i] = new Chromosome(jobs, r);
            parents[i].fitness = 1.0 / c.evaluate(parents[i], input, operationMatrix);
        }

        children = new Chromosome[this.popSize];
        for (int i = 0; i < this.popSize; i++) {
            children[i] = new Chromosome(parents[i]);
        }

        // 获取最优子代
        double maxFitness = Double.NEGATIVE_INFINITY;
        int index = 0;
        for (int i = 0; i < this.popSize; i++) {
            if (maxFitness < parents[i].fitness) {
                index = i;
                maxFitness = parents[i].fitness;
            }
        }
        best = new Chromosome(parents[index]);
        currentBest = new Chromosome(parents[index]);
    }

    public boolean newGeneration() {

        if(gen > maxGen) return false;

        // 陷入局部最优时进行扰动:取部分精英个体后随机生成新个体
        /*double fr = calcFrequeny();
        double hmg = calcHammingDistance();
        double l = currentBest.gene_OS.length + currentBest.gene_MS.length;
        this.pm = Math.abs(0.5 * Math.exp(-6.2146 * currentBest.fitness));
        System.out.println("P_m=" + this.pm);
        double pp = Math.abs((2d - 1d) / 2d - (l * this.pm)) / ((l / (l - 1d)) * hmg * (1d - fr));
        this.pc = pp == Double.NEGATIVE_INFINITY ? 0.0026857308097803768 : pp;
        System.out.println("P_c=" + this.pc);*/
//                break;
        if (gen - noImprove > this.maxStagnantStep) {
            int num = (int) (pp * popSize);
            ArrayList<Chromosome> p = new ArrayList<>();
            Collections.addAll(p, parents);
            Collections.sort(p);
            for (int i = 0; i < num; i++)
                parents[i] = p.get(i);
            for (int i = num; i < this.popSize; i++) {
                parents[i] = new Chromosome(jobs, r);
                parents[i].fitness = 1.0 / c.evaluate(parents[i], input, operationMatrix);
            }

            noImprove = gen;
        }
        // 选择 selection
        /*double sum = 0;
        for (int i = 0; i < this.popSize; i++) {
            sum += children[i].fitness;
        }
        pr = currentBest.fitness / sum;//this.m * gen + 0.1;
        System.out.println("P_r=" + pr);*/
        children = chromOps.Selection(parents, pr);

        // 交叉 cross
        for (int i = 0; i < this.popSize; i += 2) {
            if (r.nextDouble() < this.pc) {
//					int fatherIndex = r.nextInt(popSize);
//					int motherIndex = r.nextInt(popSize);
//					while (fatherIndex == motherIndex)
//						motherIndex = r.nextInt(popSize);
                int fatherIndex = i;
                int motherIndex = i + 1;
                chromOps.Crossover(children[fatherIndex], children[motherIndex]);
            }
        }

        // 变异 mutation //Hesser.1991
        for (int i = 0; i < this.popSize; i++) {
            if (r.nextDouble() < this.pm) {
                chromOps.Mutation(children[i]);
            }
        }

        // update fitness
//            for (int i = 0; i < this.popSize; i++){
//                children[i].fitness = 1.0 / c.evaluate(children[i], input, operationMatrix);
//                parents[i] = new Chromosome(children[i]);
//            }
        for (int i = 0; i < this.popSize; i++) {
            children[i].fitness = 1.0 / c.evaluate(children[i], input, operationMatrix);
            int maxTSIterSize = (int) (maxGen * ((float) gen / (float) maxGen));
            Solution sol = new Solution(operationMatrix, children[i], input, 1.0 / children[i].fitness);

            // TS1
            sol = NeiborAl2.search(sol, maxTSIterSize);

            // TS2
//                sol = NeighbourAlgorithms.neighbourSearch(sol);

            parents[i] = sol.toChromosome();
        }

        // get best chromosome
        currentBest = getBest(parents);

//            ArrayList<Chromosome> p = new ArrayList<>();
//            Collections.addAll(p, children);
//            Collections.sort(p);
//            currentBest = p.get(0);
//
//             tabu search
//            int tabuNr = popSize - (int) (pt * popSize);
//            for (int i = 0; i < tabuNr; i++) {
//                parents[i] = new Chromosome(children[i]);
//            }
//            for (int i = tabuNr; i < popSize; i++) {
//                int maxTSIterSize = (int) (maxGen * ((float) gen / (float) maxGen));
//                parents[i] = new Chromosome(tabu.TabuSearch(maxTSIterSize, 5, children[i], best.fitness));
//            }


        if (best.fitness < currentBest.fitness) {
            best = new Chromosome(currentBest);
            noImprove = gen;
        }

        gen++;
        bestSolution = new Solution(operationMatrix, best, input, c.evaluate(best, input, operationMatrix));

        return true;
    }

    private double calcHammingDistance() {
        double distance = Double.MAX_VALUE;
        for (int i = 0; i < this.popSize; i++) {
            double tmp = 0;
            for (int j = i + 1; j < this.popSize; j++) {
                for (int i1 = 0; i1 < children[i].gene_OS.length; i1++) {
                    if (children[i].gene_OS[i1] != children[j].gene_OS[i1]) {
                        tmp++;
                    }
                }
                for (int i1 = 0; i1 < children[i].gene_MS.length; i1++) {
                    if (children[i].gene_MS[i1] != children[j].gene_MS[i1]) {
                        tmp++;
                    }
                }
            }
            for (int m = i - 1; m > 0; m--) {
                for (int i1 = 0; i1 < children[i].gene_OS.length; i1++) {
                    if (children[i].gene_OS[i1] != children[m].gene_OS[i1]) {
                        tmp++;
                    }
                }
                for (int i1 = 0; i1 < children[i].gene_MS.length; i1++) {
                    if (children[i].gene_MS[i1] != children[m].gene_MS[i1]) {
                        tmp++;
                    }
                }
            }
            if (tmp < distance) {
                distance = tmp;
            }
        }
        return distance;
    }

    private double calcFrequeny() {
        double fr = 1;
        for (int i = 0; i < this.popSize; i++) {
            if (currentBest.compareTo(children[i]) == 0) {
                fr++;
            }
        }
        return fr/this.popSize;
    }

    private Chromosome getBest(Chromosome[] parents) {
        double maxFitness = Double.NEGATIVE_INFINITY;
        int index = 0;
        for (int i = 0; i < this.popSize; i++) {
            if (maxFitness < parents[i].fitness) {
                index = i;
                maxFitness = parents[i].fitness;
            }
        }
        return new Chromosome(parents[index]);
    }

    public Solution getBestSolution() {
        return bestSolution;
    }
}