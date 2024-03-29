package src.swe.smft.program;

import src.swe.smft.event.*;
import src.swe.smft.event.Event;
import src.swe.smft.plot.HarryPlotter;
import src.swe.smft.memory.DataCenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        HarryPlotter hp = HarryPlotter.getInstance();
        TreeManager tm = new TreeManager();
        EventFactory modeler = EventFactory.getInstance();
        DataCenter dc = new DataCenter();
        Simulator sim;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Utility di creazione ed analisi di modelli SMFT:\nSi desidera usare un modello preimpostato [P]" +
                " o un modello casuale[r]? ");
        boolean premadeModel = true;

        if (scanner.next().equals("r")){
            premadeModel = false;
            // random flow (default)
            int nBasic = 10;
            System.out.println("Digitare numero di foglie desiderato (>= 2)["+nBasic+"]: ");
            try {
                nBasic = Integer.parseInt(scanner.next());
                if(nBasic < 2) {
                    System.err.println("Numero insufficiente, verrà utilizzato il valore di default");
                }
            } catch(NumberFormatException e) {
                System.err.println("Non è stato digitato un numero intero, verrà utilizzato il valore di default");
            }


            List<Event> topChildren = new ArrayList<>();
            boolean[] chosenBasic = new boolean[nBasic];

            for (int i = 0; i < nBasic; i++) {
                BasicEvent e = (BasicEvent) modeler.createRandomBasicEvent();
                tm.addBasicEvent(e);
                chosenBasic[i] = false;
            }

            for (int j = 0; j < nBasic / 2; j++) {
                List<Event> children = new ArrayList<>();
                for (int k = 0; k < (int) (Math.random() * (nBasic - 1) + 2); k++) {
                    int choose = (int) (Math.random() * nBasic);
                    children.add(tm.getBasicEvents().get(choose));
                    chosenBasic[choose] = true;
                }
                // j-esimo IntermediateEvent
                Event i = modeler.createRandomIntermediateEvent(children);
                topChildren.add(i);
            }

            for (int i = 0; i < nBasic; i++) {
                if (!chosenBasic[i]) {
                    Event e = tm.getBasicEvents().get(i);
                    topChildren.add(e);
                }
            }

            // dato che con topEvent != da K=runs/2 la simulazione non risulta interessante
            // Event topEvent = modeler.createRandomIntermediateEvent(topChildren);
            String topOpz = String.valueOf(topChildren.size() / 2);
            Event topEvent = modeler.createIntermediateEvent(topChildren, topOpz);

            tm.setTopEvent((IntermediateEvent) topEvent);
        }
        else {
            float lambdaA = 0.7f;
            float muA = 0.3f;
            boolean statusA = true;
            Event A = modeler.createBasicEvent(lambdaA, muA, statusA);
            tm.addBasicEvent((BasicEvent) A);

            float lambdaB = 0.4f;
            float muB = 0.6f;
            boolean statusB = true;
            Event B = modeler.createBasicEvent(lambdaB, muB, statusB);
            tm.addBasicEvent((BasicEvent) B);

            float lambdaC = 0.7f;
            float muC = 0.3f;
            boolean statusC = true;
            Event C = modeler.createBasicEvent(lambdaC, muC, statusC);
            tm.addBasicEvent((BasicEvent) C);

            float lambdaD = 0.4f;
            float muD = 0.6f;
            boolean statusD = true;
            Event D = modeler.createBasicEvent(lambdaD, muD, statusD);
            tm.addBasicEvent((BasicEvent) D);

            List<Event> childrenE = List.of(A, B, C);
            List<Event> childrenF = List.of(B, C, D);
            String opzE = "AND";
            String opzF = "SEQAND";
            Event E = modeler.createIntermediateEvent(childrenE, opzE);
            Event F = modeler.createIntermediateEvent(childrenF, opzF);

            List<Event> childrenTop = List.of(E, F);
            String opzTop = "OR";
            Event top = modeler.createIntermediateEvent(childrenTop, opzTop);
            tm.setTopEvent((IntermediateEvent) top);
        }

        float maxTime = 15;
        System.out.println("Digitare la durata delle simulazioni desiderata (>=0)["+maxTime+"]: ");
        try {
            maxTime = Float.parseFloat(scanner.next());
            if(maxTime < 0) {
                System.err.println("La durata delle simulazioni non può essere negativa, verrà utilizzato il valore di default");
            }
        } catch(NumberFormatException e) {
            System.err.println("Non è stato digitato un numero, verrà utilizzato il valore di default");
        }
        //elimino le cifre decimali in eccesso
        int aux = (int)(maxTime * 10);
        maxTime = aux/10f;

        sim = new Simulator(maxTime, tm);
        Analyzer analyzer = new Analyzer(sim, dc);

        int runs = 100000;
        System.out.println("Digitare il numero di simulazioni per test desiderato (deve essere multiplo di 10) ["+runs+"]: ");

        try {
            runs = Integer.parseInt(scanner.next());
            if(runs < 1 || runs % 10 != 0) {
                System.err.println("Numero non adeguato, verrà utilizzato il valore di default");
            }
        } catch(NumberFormatException e) {
            System.err.println("Non è stato digitato un numero, verrà utilizzato il valore di default");
        }

        System.out.println("Digitare il valore del passo di campionamento desiderato [0.1]. Nota, deve dividere " + maxTime);
        float quantum;
        try {
            quantum = Float.parseFloat(scanner.next());
            if(quantum < 0 || maxTime / quantum != (int) maxTime / quantum) {
                System.err.println("Passo digitato non adeguato, verrà utilizzato il valore di default");
                quantum = .1f;
            }
        } catch(NumberFormatException e) {
            System.err.println("Non è stato digitato un numero, verrà utilizzato il valore di default");
            quantum = .1f;
        }

        boolean doCI = true;
        boolean doErgodic = true;

        int selection;
        System.out.println("Cosa si desidera eseguire?\n -1: Definizione Intervalli di confidenza per il valore atteso della media campionaraia" +
                "\n -2: Valutazione natura Ergodica del modello\n -3: Entrambi (default)");

        try {
            selection = Integer.parseInt(scanner.next());
        } catch(NumberFormatException e) {
            System.err.println("Non è stato digitato un numero, verrà utilizzato il valore di default");
            selection = 3;
        }

        // questi devono essere i valori di default
        float alpha = 0.05f;
        boolean meanPLot = true;
        boolean faultPLot = false;
        // questi due devono essere facilmente cambiabili
        double stDeviationPrecision = 0.45;
        double meanPrecision = 0.3;

        if(selection == 1 || selection == 3) {
            System.out.println("Digitare il valore di significatività desiderato");
            try {
                float temp = Float.parseFloat(scanner.next());
                if(temp <= 0 || temp >= 1) {
                    System.err.println("Valore di significatività non accettabile, verrà utilizzato il valore di default");
                } else alpha = temp;
            } catch(NumberFormatException e) {
                System.err.println("Non è stato digitato un numero, verrà utilizzato il valore di default");
            }

            System.out.println("Si desidera visualizzare la media campionaria dello stato di funzionamento del top event? Y/n ");
            if(scanner.next().equals("n")) meanPLot = false;
            else
                System.err.println("Digitazione errata, verrà utilizzato il valore di default");

            System.out.println("Si desidera visualizzare la media campionaria dello stato di mal-funzionamento del top event? y/N ");
            if(scanner.next().equals("y")) faultPLot = true;
            else
                System.err.println("Digitazione errata, verrà utilizzato il valore di default");
        }

        if(selection == 2 || selection == 3) {
            System.out.println("Digitare la precisione della deviazione standard desiderata (>0) ["+stDeviationPrecision+"]: ");
            try {
                double tmp = Float.parseFloat(scanner.next());
                if(tmp <= 0)
                    System.err.println("Deve essere positiva, verrà utilizzato il valore di default");
                else stDeviationPrecision = tmp;
            } catch(NumberFormatException e) {
                System.err.println("Non è stato digitato un numero, verrà utilizzato il valore di default");
            }

            System.out.println("Digitare la precisione della media campionaria desiderata (>0) ["+meanPrecision+"]: ");
            try {
                double tmp = Float.parseFloat(scanner.next());
                if(tmp <= 0)
                    System.err.println("Deve essere positiva, verrà utilizzato il valore di default");
                else meanPrecision = tmp;
            } catch(NumberFormatException e) {
                System.err.println("Non è stato digitato un numero, verrà utilizzato il valore di default");
            }
        }

        hp.printGraph();

        if (doCI) {
            hp.printCIInfo(premadeModel, tm.getBasicEvents().size(), maxTime, runs, quantum, alpha, meanPLot, faultPLot);
            analyzer.defineCI(runs, alpha, quantum, meanPLot, faultPLot);
        }
        if (doErgodic) {
            hp.printErgodicInfo(premadeModel, tm.getBasicEvents().size(), maxTime, runs, quantum, meanPrecision, stDeviationPrecision);
            analyzer.verifyErgodic(runs, quantum, meanPrecision, stDeviationPrecision);
        }
    }
}