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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The (progressively growing) class Tools includes several practical methods that
 * are commonly used in NLP applications.
 *
 * @author Meghdad Farahmand
 * @since 27.1.2015
 *
 */
public class Tools {
    /**
     * This method can be used in the following cases: when an ngram has
     * different POS tags, the method returns the tag combination with the highest
     * frequency. 
     * 
     * Input is a HashMap of POS tagged ngrams and their frequencies.
     * Format of input entries: state_NN police_NN 23
     *
     * @param input Map of POS tagged ngrams and their frequencies
     * @return Map of ngrams tagged with their most frequent POS tag and maximum
     * frequency.
     */
    public HashMap MostFrequentPosTagNgram(HashMap<String, Integer> input) {

        System.out.println();
        System.out.println("Method: Tools.MostFrequentPosTagNgram()");
        System.out.println();

        System.out.println("Finding Ngrams with the most frequent POS tags...");
        System.out.println();

        //output
        HashMap<String, Integer> outMap = new HashMap<String, Integer>();
        //HashMap of ngrams and their list of observed tags
        HashMap<String, TaggedNgram> hc = new HashMap<String, TaggedNgram>();

        Pattern splitNgram = Pattern.compile("(\\w+)_(\\w+)");

        int counter = 0;

        for (String k : input.keySet()) {

            //System.out.println(k);
            counter++;
            if (counter % 100000 == 0) {
                System.out.println(counter);
            }

            //strip off the tags from ngram
            String unTaggedNgram = "";
            String posTags = "";
            Matcher splitMatcher = splitNgram.matcher(k);
            while (splitMatcher.find()) {
                unTaggedNgram = unTaggedNgram.concat(splitMatcher.group(1) + " ");
                posTags = posTags.concat(splitMatcher.group(2) + " ");
            }

            //remove trailing spcae:
            unTaggedNgram = unTaggedNgram.trim();
            posTags = posTags.trim();

            //populate hc:
            if (!hc.containsKey(unTaggedNgram)) {
                TaggedNgram c1 = new TaggedNgram();
                c1.getTags().add(posTags);
                c1.getTagFrequency().add(input.get(k));
                hc.put(unTaggedNgram, c1);
            } else {
                TaggedNgram c2 = hc.get(unTaggedNgram);
                c2.getTags().add(posTags);
                c2.getTagFrequency().add(input.get(k));
                hc.put(unTaggedNgram, c2);
            }
        }

        for (String ngr : hc.keySet()) {

            //for this key (ngr) find the pos tag with the highest frequency
            int ind_max = 0;

//            System.out.println(ngr);
            if (hc.get(ngr).getTags().size() > 1) {
                System.out.println(ngr);
                for (int i = 0; i < hc.get(ngr).getTags().size(); i++) {

                    System.out.println(hc.get(ngr).getTags().get(i) + " --> " + hc.get(ngr).getTagFrequency().get(i));
                    if (hc.get(ngr).getTagFrequency().get(i) > hc.get(ngr).getTagFrequency().get(ind_max)) {
                        ind_max = i;
                    }
                }

                System.out.println("Choosing: " + hc.get(ngr).getTags().get(ind_max) + ": " + hc.get(ngr).getTagFrequency().get(ind_max));
                System.out.println("-----------------------------------");

            }

            System.out.println("SIZE OF HC: " + hc.size());

            //reproduce the POS tagged compound with most frequent tags
            String[] tags = hc.get(ngr).getTags().get(ind_max).split(" ");

            String newKey = "";
            String[] terms = ngr.split(" ");
            //terms[0] + "_" + tags[0] + " " + terms[1] + "_" + tags[1];
            for (int i = 0; i < terms.length; i++) {
                newKey += terms[i] + "_" + tags[i] + " ";
            }
//            
            newKey = newKey.trim();
//            System.out.println("return: "+newKey);

            outMap.put(newKey, hc.get(ngr).getTagFrequency().get(ind_max));
        }
        return outMap;
    }

    /**
     *
     * Method to merge the POS tags of the input HashMap of pos tagged word
     * pairs and their frequency.
     *
     * The method can be used to merge the compounds that are tagged
     * differently. e.g. bank_NN account_NN or bank_NN account_NNP
     *
     * @param input HashMap<String, Integer>: asphalt_nn emulsions_nns 2
     * @return HashMap<String, Integer>: asphalt emulsions 2
     */
    public HashMap MergePosTags(HashMap<String, Integer> input) {

        //output
        HashMap<String, Integer> outMap = new HashMap<String, Integer>();

        for (String k : input.keySet()) {
            String[] elements = k.split("[ _]");

            String pairForm = elements[0] + " " + elements[2];
            int pairFreq = input.get(k);

            if (!outMap.containsKey(pairForm)) {
                outMap.put(pairForm, pairFreq);
            } else {
                int prevFreq = outMap.get(pairForm);
                int newFreq = prevFreq + pairFreq;
                outMap.put(pairForm, newFreq);
            }
        }
        return outMap;
    }
    /**
     * This method can be used to extract the lexicon (unigrams) from corpus. If
     * the corpus that the program receives as input is not pos tagged, a flag
     * should be passed as an argument indicating this. Otherwise the program
     * assumes that the corpus is pos tagged.
     *
     * @param p2corpus: path to corpus
     * @param lexFreqThreshold: frequency threshold on extracted unigrams
     * @param isPosTagged: whether (true) or not (false) the corpus is pos
     * tagged.
     * @param ignoreCase: whether (true) or not (false) ignore case.
     *
     * @return list containing plain and pos tagged (if available) lexicons. For
     * each entry of the lexicon frequency is also returned.
     */
    public List<HashMap<String, Integer>> ExtractUnigram(String p2corpus, int lexFreqThreshold, boolean isPosTagged, boolean ignoreCase) throws UnsupportedEncodingException, FileNotFoundException, IOException {


        int freqThreshold = lexFreqThreshold;

        HashMap<String, Integer> allNgs = new HashMap();
        HashMap<String, Integer> allNgsPos = new HashMap();

        HashMap<String, Integer> retNgs = new HashMap<String, Integer>();
        HashMap<String, Integer> retNgsPos = new HashMap<String, Integer>();

        List<HashMap<String, Integer>> ret = new ArrayList();

        //reading corpus
        BufferedReader corpus = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(p2corpus), "UTF8"));

//        System.out.println("Creating the lexicon (with word counts)...");
        //if the input corpsu is pos tagged (default)



        if (isPosTagged) {
            String sentence="";
            int c = 0;
            String[] words_pos;
            int tmp = 0;

            while ((sentence = corpus.readLine()) != null) {
                if (ignoreCase) {
                    sentence = sentence.toLowerCase();

                    //show how many lines have been processed
                    c++;
                    if ((c % 1000000) == 0) {
                        System.out.println("Processing line: " + c);
                    }
                }

                words_pos = sentence.split(" ");
                for (String wp : words_pos) {
                    //HashMap with POS tags
                    if (!allNgsPos.containsKey(wp)) {
                        allNgsPos.put(wp, 1);
                    } else {
                        tmp = allNgsPos.get(wp);
                        allNgsPos.put(wp, ++tmp);
                    }

                    //HashMap without POS tags
                    String[] wpArray = wp.split("_");
                    if (!allNgs.containsKey(wpArray[0])) {
                        allNgs.put(wpArray[0], 1);
                    } else {
                        tmp = allNgs.get(wpArray[0]);
                        allNgs.put(wpArray[0], ++tmp);
                    }
                }
            }

            //apply freq threshold to allNgs and allNgsPos
            for (String ng : allNgs.keySet()) {
                if (allNgs.get(ng) >= lexFreqThreshold) {
                    retNgs.put(ng, allNgs.get(ng));
                }
            }
            for (String ng : allNgsPos.keySet()) {
                if (allNgsPos.get(ng) >= lexFreqThreshold) {
                    retNgsPos.put(ng, allNgsPos.get(ng));
                }
            }

            //if the corpus is not pos tagged
        } else if (!isPosTagged) {

            String sentence;
            String[] words_pos;
            int tmp = 0;
            int c = 0;
            while ((sentence = corpus.readLine()) != null) {
                if (ignoreCase) {
                    sentence = sentence.toLowerCase();
                    c++;
                    if ((c % 1000000) == 0) {
                        System.out.println("Processing line: " + c);
                    }
                }
                words_pos = sentence.split(" ");
                for (String wp : words_pos) {

                    if (!allNgs.containsKey(wp)) {
                        allNgs.put(wp, 1);
                    } else {
                        tmp = allNgs.get(wp);
                        allNgs.put(wp, ++tmp);
                    }
                }

            }

            //apply freq threshold
            for (String ng : allNgs.keySet()) {
                if (allNgs.get(ng) >= lexFreqThreshold) {
                    retNgs.put(ng, allNgs.get(ng));
                }
            }

        }

        //first index of ret is the list of unigrams
        //second index of ret is the list of postagged unigrams (if available)
        ret.add(retNgs);
        ret.add(retNgsPos);

        return ret;
    }





    /**
     *
     * Extract the list of n-grams from (pos-tagged) corpus. Generates either
     * pos-tagged or plain n-grams.
     *
     * @param p2corpus path to corpus
     * @param freqThreshold n-grams below this threshold will be discarded in
     * the output.
     * @param order order (n) of the n-gram
     * @param isCorpusPosTagged is the input corpus pos-tagged?
     * @param ignoreCase ignore case?
     * @param outputPosTagged should the output be pos-tagged or plain n-grams?
     *
     * @return HashMap containing plain OR pos tagged n-grams
     *
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public HashMap<String, Integer> ExtractNgrams(String p2corpus, int freqThreshold, int order, boolean isCorpusPosTagged, boolean outputPosTagged, boolean ignoreCase) throws UnsupportedEncodingException, FileNotFoundException, IOException {

        if (isCorpusPosTagged) {
//            System.out.println("Corpus format: POS Tagged");
        } else if (!isCorpusPosTagged) {
//            System.out.println("Corpus format: Plain Text");
        }
//        System.out.println("Frequency threshold: " + freqThreshold);
//        System.out.println("N = "+order);

        HashMap<String, Integer> allNgs = new HashMap();
        HashMap<String, Integer> returnNgs = new HashMap();

        List<HashMap<String, Integer>> ret = new ArrayList();

        //reading corpus
        BufferedReader corpus = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(p2corpus), "UTF8"));

        /*
        while can iterate very many times because the number of lines can get really
        huge. Therefore, as much as possible, I declare all variables used inside the
        loop outside the loop.
        */
        String sentence;
        int c = 0;
        String[] unigrams;
        String ng = "";
        List<String> ngs = new ArrayList<>();
        StringBuilder ngStr = new StringBuilder();

        long start = System.nanoTime();
        while ((sentence = corpus.readLine()) != null) {

            if (ignoreCase) {
                sentence = sentence.toLowerCase();
            }

            //Extract n-grams from sentence
            //for each n-gram extracted by NgExtract ReturnNgrams() method
            //NgExtract inter = new NgExtract();
            //for (String ng : inter.ReturnNgrams(order, sentence)){
            //}
            //more memory efficient approach to extract n-grams from sentence
            unigrams = sentence.split(" ");
            if (unigrams.length >= 300) {
                System.out.println(" long sentence (length >= 300 words) detected at line: "+c);
            }


            //extract all n-grams from sentence:
            for (int i = 0; i < unigrams.length - order + 1; i++) {


                for (int j = i; j < i + order; j++) {
                    ngStr.append(j > i ? " " : "").append(unigrams[j]); //changed this from + to chain of append
                }
                ng = ngStr.toString();
                ngStr.setLength(0);
                if (!ngs.contains(ng.trim())) {
                    ngs.add(ng.trim());
                }
            }


            //transform the list of all n-grams to HashMap of n-grams and their count
            for (String ngram : ngs) {


                if (!allNgs.containsKey(ngram)) {
                    allNgs.put(ngram, 1);
                } else if (allNgs.containsKey(ngram)) {
                    int tmp = allNgs.get(ngram);
                    allNgs.put(ngram, ++tmp);
                }
            }
            ngs.clear();
            c++;
            if ((c % 1000000) == 0) {


                System.out.println("Processing Line: " + c);

                //long estimatedTime = System.nanoTime() - start;
                //start = System.nanoTime();
                //force garbage collection
                //System.gc();
            }
        }

        Pattern p = Pattern.compile("(\\w+)_\\w+\\s(\\w+)_\\w+");

        if (outputPosTagged && isCorpusPosTagged) {
            returnNgs = allNgs;
        } else if (!outputPosTagged && isCorpusPosTagged) {

            for (String ngpos : allNgs.keySet()) {
                Matcher m = p.matcher(ngpos);
                if (m.find()) {
                    if (!returnNgs.containsKey(m.group(1) + " " + m.group(2))) {
                        returnNgs.put(m.group(1) + " " + m.group(2), 1);
                    } else {
                        int tmp = returnNgs.get(m.group(1) + " " + m.group(2));
                        returnNgs.put(m.group(1) + " " + m.group(2), ++tmp);
                    }
                }
            }
        } else if (outputPosTagged && !isCorpusPosTagged) {
            System.out.println("Can't return postagged n-grams from un-tagged corpus. Returning plain n-grams.");
            returnNgs = allNgs;
        } else if (!outputPosTagged && !isCorpusPosTagged) {
            returnNgs = allNgs;
        }

        //apply frequency threshold
        System.out.println("Applying frequency threshold...");
        Iterator it1 = returnNgs.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry<String, Integer> ent1 = (Map.Entry) it1.next();
            if (ent1.getValue() < freqThreshold) {
                it1.remove();
            }
        }

        System.out.println("Returning Map of Ngramgs.");
        return returnNgs;

    }




    /**
     * The following method extracts noun compounds from the corpus. It is
     * possible for this method to extract other syntactic patterns such as:
     * adjective noun, verb particle etc. For the time being the pattern of the
     * compounds can be passed as argument. The available patterns are: 1. nn-nn
     * 2. jj-nn
     *
     *
     * @param p2corpus path to corpus
     * @param SyntacticPattern syntactic pattern of the word sequence to be
     * extracted.
     * @param igCase whether or not ignore case
     *
     * @return Map of compounds (or other word sequences) and their frequencies.
     */
    public HashMap extractNCs(String p2corpus, String SyntacticPattern, boolean igCase, boolean withPOS, int freqThreshold) throws FileNotFoundException, IOException {


        BufferedReader corpus = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(p2corpus)));

        HashMap<String, Integer> NCs = new HashMap<String, Integer>();
        //HashMap<String, Integer> NCsMostFreqPos = new HashMap<String, Integer>();

        Pattern cmpPat = null;

        if (SyntacticPattern.equals("nn-nn")) {
            //using positive lookahead: (?=mypattern) identifies the overlapping patterns:
            //car_NN engine_NN mechanic_NN --> car_NN engine_NN, engine_NN mechanic_NN
            //without positive lookahead: car_NN engine_NN
            cmpPat = Pattern.compile("(?=\\b(\\p{Alpha}+)_([Nn][Nn][Ss]?)\\s(\\p{Alpha}+)_([Nn][Nn][Ss]?)\\b).");
        } else if (SyntacticPattern.equals("jj-nn")) {
            cmpPat = Pattern.compile("(?=\\b(\\w+)_(JJ(R|S)?)\\s(\\w+)_(NNS?)\\b).");
        } else {
            System.out.println("Error: MWE pattern not recognized. Returning null.");
            return null;
        }


        /*
        while can iterate very many times because the number of lines can get really 
        huge. Therefore, I declare all variable used inside the 
        loop outside the loop. 
        */
        int sent_count = 0;
        String l = "";
        String cat = "";
        Matcher m_nc;
        int tmp_count_nc=0;
        while ((l = corpus.readLine()) != null) {
//            sent_count++;
//            if ((sent_count % 1000000) == 0) {
//                System.out.println("Processing line: " + sent_count);
//            }
            m_nc = cmpPat.matcher(l);
            while (m_nc.find()) {

                cat = "";

                //output with POS tags
                if (withPOS) {

                    cat = m_nc.group(1) + "_" + m_nc.group(2) + " " + m_nc.group(3) + "_" + m_nc.group(4);

                    //output w/o POS tags
                } else {
                    cat = m_nc.group(1) + " " + m_nc.group(3);
                }

                if (igCase) {
                    cat = cat.toLowerCase();
                }

                if (!NCs.containsKey(cat)) {
                    NCs.put(cat, 1);
                } else if (NCs.containsKey(cat)) {
                    tmp_count_nc = NCs.get(cat);
                    tmp_count_nc++;
                    NCs.put(cat, tmp_count_nc);
                }
            }
        }

        //filtering based on frequency:
        if (freqThreshold > 1) {
            Iterator<Entry<String, Integer>> it = NCs.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Integer> entry = it.next();
                if (entry.getValue() < freqThreshold) {
                    it.remove();
                }
            }
        }
        return NCs;
    }
}
