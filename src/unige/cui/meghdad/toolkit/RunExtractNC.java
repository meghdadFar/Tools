

/* 
 * Copyright (C) 2016 Meghdad Farahmand<meghdad.farahmand@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unige.cui.meghdad.toolkit;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Meghdad Farahmand<meghdad.farahmand@gmail.com>
 */
public class RunExtractNC {

    public static void main(String[] args) throws ParseException, IOException {

        //using apache commons CLI to parse command line arguments
        // create Options object
        Options options = new Options();

        //required options
        options.addOption("p2corpus", true, "Path 2 Corpus");

        options.addOption("p2outdir", true, "Path 2 Output Directory");

        options.addOption("fth", true, "Frequency threshold");

        options.addOption("synPattern", true, "nn-nn, jj-nn, etc.");

        options.addOption("outputPOS", true, "Output with or without pos tags");

        options.addOption("igcase", true, "Whether or not ignore case");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        boolean igcase = false;
        if (cmd.getOptionValue("igcase").equals("1")) {
            igcase = true;
        }

        boolean outputPOS = false;
        if (cmd.getOptionValue("outputPOS").equals("1")) {
            outputPOS = true;
        }

        int freqTh = Integer.parseInt(cmd.getOptionValue("fth"));

        //create an object of type tools for later usage
        Tools T = new Tools();

        HashMap<String, Integer> twoNCS = T.extractNCs(cmd.getOptionValue("p2corpus"), cmd.getOptionValue("synPattern"), igcase, outputPOS, freqTh);

        //handle the compounds with different tag pairs. There are two solutions:
        //1. merge tags by Tools.MergePosTags(). This can be used e.g., for merging NNP, NNS and NN --> NN
        //2. choose the most frequent tag-pair by Tools.MostFrequentPosTagNgram()
        HashMap<String, Integer> twoNCSMerged = T.MergePosTags(twoNCS);

        //create output file
        Writer wr = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(cmd.getOptionValue("p2outdir") + "/ncs.gt" + cmd.getOptionValue("fth") + ".txt"), "UTF-8"));

        for (String s : twoNCSMerged.keySet()) {

            wr.write(s + " " + twoNCSMerged.get(s) + "\n");

        }
        wr.flush();
        wr.close();
    }
}
