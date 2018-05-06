/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.hotspots

import org.chronolens.core.cli.Subcommand
import org.chronolens.core.cli.exit
import org.chronolens.core.serialization.JsonModule
import org.chronolens.hotspots.HistoryAnalyzer.Metric
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
    name = "hotspots",
    description = [
        "Loads the persisted repository, detects the hotspots of the system " +
            "and reports the results to the standard output."
    ],
    showDefaultValues = true
)
class HotspotsCommand : Subcommand() {
    override val name: String get() = "hotspots"

    @Option(
        names = ["--metric"],
        description = ["the metric used to rank and highlight source nodes"]
    )
    private var metric: Metric = Metric.WEIGHTED_CHURN

    @Option(
        names = ["--skip-days"],
        description = [
            "when analyzing source nodes, ignore revisions occurring in the " +
                "first specified number of days, counting from the revision " +
                "when the source node was created."
        ]
    )
    private var skipDays: Int = 14

    @Option(
        names = ["--min-metric-value"],
        description = [
            "ignore source files that have less churn than the specified limit"
        ]
    )
    private var minMetricValue: Int = 0

    private fun validateOptions() {
        if (skipDays < 0) exit("skip-days can't be negative!")
        if (minMetricValue < 0) exit("min-metric-value can't be negative!")
    }

    override fun run() {
        validateOptions()
        val analyzer = HistoryAnalyzer(metric, skipDays)
        val repository = load()
        val report = analyzer.analyze(repository.getHistory())
        val files = report.files.filter { it.value >= minMetricValue }
        JsonModule.serialize(System.out, files)
    }
}
