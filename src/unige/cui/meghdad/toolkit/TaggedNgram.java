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

import java.util.ArrayList;
import java.util.List;

/**
 * Class to keep ngrams and their different tag sequences
 * and frequency of those tag sequences. 
 * 
 * @author Meghdad Farahmand
 */

class TaggedNgram {

    private List<String> tags = new ArrayList();
    private List<Integer> tagFrequency = new ArrayList();

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @param tags 
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * @return the tagFrequency
     */
    public List<Integer> getTagFrequency() {
        return tagFrequency;
    }

    /**
     * @param tagFrequency set tagFrequency
     */
    public void setTagFrequency(List<Integer> tagFrequency) {
        this.tagFrequency = tagFrequency;
    }

}
