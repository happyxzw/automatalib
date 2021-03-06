/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.util.graphs.scc;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.commons.util.Holder;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.util.graphs.traversal.GraphTraversalAction;
import net.automatalib.util.graphs.traversal.GraphTraversalVisitor;

/**
 * Depth-first traversal visitor realizing Tarjan's algorithm for finding all strongly-connected components (SCCs) in a
 * graph.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public class TarjanSCCVisitor<N, E> implements GraphTraversalVisitor<N, E, TarjanSCCRecord> {

    private static final int NODE_FINISHED = -1;
    private final MutableMapping<N, TarjanSCCRecord> records;
    private final List<TarjanSCCRecord> currentScc = new ArrayList<>();
    private final List<N> currentSccNodes = new ArrayList<>();
    private final SCCListener<N> listener;
    private int counter;

    /**
     * Constructor.
     *
     * @param graph
     *         the graph
     * @param listener
     *         the SCC listener to use, <b>may not be null</b>
     */
    public TarjanSCCVisitor(Graph<N, E> graph, SCCListener<N> listener) {
        records = graph.createStaticNodeMapping();
        this.listener = listener;
    }

    @Override
    public GraphTraversalAction processInitial(N initialNode, Holder<TarjanSCCRecord> outData) {
        outData.value = createRecord();
        return GraphTraversalAction.EXPLORE;
    }

    @Override
    public boolean startExploration(N node, TarjanSCCRecord data) {
        records.put(node, data);
        return true;
    }

    @Override
    public void finishExploration(N node, TarjanSCCRecord data) {
        currentScc.add(data);
        currentSccNodes.add(node);
        if (data.lowLink == data.number) {
            for (TarjanSCCRecord tr : currentScc) {
                tr.lowLink = NODE_FINISHED;
            }
            listener.foundSCC(currentSccNodes);
            currentScc.clear();
            currentSccNodes.clear();
        }
    }

    @Override
    public GraphTraversalAction processEdge(N srcNode,
                                            TarjanSCCRecord srcData,
                                            E edge,
                                            N tgtNode,
                                            Holder<TarjanSCCRecord> dataHolder) {
        TarjanSCCRecord rec = records.get(tgtNode);
        if (rec == null) {
            rec = createRecord();
            dataHolder.value = rec;
            return GraphTraversalAction.EXPLORE;
        }

        if (rec.lowLink != NODE_FINISHED) {
            int tgtNum = rec.number;
            if (tgtNum < srcData.lowLink) {
                srcData.lowLink = tgtNum;
            }
        }
        return GraphTraversalAction.IGNORE;
    }

    @Override
    public void backtrackEdge(N srcNode, TarjanSCCRecord srcData, E edge, N tgtNode, TarjanSCCRecord tgtData) {
        int tgtLl = tgtData.lowLink;
        if (tgtData.lowLink != NODE_FINISHED && tgtLl < srcData.lowLink) {
            srcData.lowLink = tgtLl;
        }
    }

    private TarjanSCCRecord createRecord() {
        return new TarjanSCCRecord(counter++);
    }

    public boolean hasVisited(N node) {
        return (records.get(node) != null);
    }

}
