package com.telmomenezes.synthetic.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.commons.cli.CommandLine;

import com.telmomenezes.synthetic.Net;
import com.telmomenezes.synthetic.Evo;
import com.telmomenezes.synthetic.Generator;
import com.telmomenezes.synthetic.samplers.DownSampler;


public class Evolve extends Command {

    @Override
    public boolean run(CommandLine cline) {
        if(!cline.hasOption("inet")) {
            setErrorMessage("input network file must be specified");
            return false;
        }
        
        if(!cline.hasOption("odir")) {
            setErrorMessage("output directory must be specified");
            return false;
        }
        
        String netfile = cline.getOptionValue("inet");
        String outdir = cline.getOptionValue("odir");
        
        boolean antiBloat = true;
        if(cline.hasOption("antibloat")) {
            if (cline.getOptionValue("antibloat").equals("off")) {
            	antiBloat = false;
            }
        }
        
        int generations = 10000;
        if(cline.hasOption("gens")) {
            generations = new Integer(cline.getOptionValue("gens"));
        }
        
        Net net = Net.load(netfile);
        
        // down sampling if needed
     	// TODO: configure attenuation and maxNodes
        int maxNodes = 999999999;
        int maxEdges = 999999999;
        if(cline.hasOption("maxnodes")) {
        	System.out.println("maxNodes: " + cline.getOptionValue("maxnodes"));
            maxNodes = new Integer(cline.getOptionValue("maxnodes"));
        }
        if(cline.hasOption("maxedges")) {
            maxEdges = new Integer(cline.getOptionValue("maxedges"));
        }
        Net sampleNet = DownSampler.sample(net, maxNodes, maxEdges);
        
     	Generator baseGenerator = null;
     	int trials = 50;
     	if(cline.hasOption("trials")) {
            trials = new Integer(cline.getOptionValue("trials"));
        }
     	boolean directed = true;
     	baseGenerator = new Generator(sampleNet.getNodeCount(), sampleNet.getEdgeCount(), directed, trials);
     	
     	int bins = 100;
     	if(cline.hasOption("bins")) {
            bins = new Integer(cline.getOptionValue("bins"));
        }
     	
        Evo evo = new Evo(sampleNet, generations, bins, baseGenerator, outdir, antiBloat);
        
        System.out.println("target net: " + netfile);
        System.out.println(evo.infoString());
        System.out.println(baseGenerator);
        
        // write experiment params to file
        try {
            FileWriter fstream = new FileWriter(outdir + "/params.txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(evo.infoString());
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        evo.run();
        
        return true;
    }
}