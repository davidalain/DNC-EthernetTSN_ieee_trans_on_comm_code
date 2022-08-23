from dataclasses import replace
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
import pandas as pd
from pathlib import Path

sns.set_theme(style="white")

current_path = Path(__file__).parent
analysesResult = pd.read_csv(current_path / "../results/AnalysesResultForPythonSeaborn.csv", sep=";")

choosen_palette="bright"


data1=analysesResult.query(
"(                          \
    (flow_name == 'tt11') & \
    (dataset_case.str.startswith(\"1-\")) & \
    (((network == 'MA1' | network == 'MA2') & (type == 'SFA_DELAY_BOUND')) | \
    ((network == 'Baseline') & (type == 'TFA_DELAY_BOUND'))) \
)"
).replace(
    ['1-1-MA1', '1-1-MA2', '1-1-Baseline', '1-2-MA1', '1-2-MA2', '1-2-Baseline', '1-3-MA1', '1-3-MA2', '1-3-Baseline', '1-4-MA1', '1-4-MA2', '1-4-Baseline'], 
    ['1', '1', '1', '2', '2', '2', '3', '3', '3', '4', '4', '4']
).sort_values(by=['network','dataset_case'], ascending=[True,True])



data2=analysesResult.query(
"                          \
    (flow_name == 'tt11') & \
    (dataset_case.str.startswith(\"2-\")) & \
    (((network == 'MA1' | network == 'MA2') & (type == 'SFA_DELAY_BOUND')) | \
    ((network == 'Baseline') & (type == 'TFA_DELAY_BOUND'))) \
"
).replace(
    ['2-1-MA1', '2-1-MA2', '2-1-Baseline', '2-2-MA1', '2-2-MA2', '2-2-Baseline', '2-3-MA1', '2-3-MA2', '2-3-Baseline', '2-4-MA1', '2-4-MA2', '2-4-Baseline'], 
    ['1', '1', '1', '2', '2', '2', '3', '3', '3', '4', '4', '4']
).sort_values(by=['network','dataset_case'], ascending=[True,True])



data3=analysesResult.query(
"(                          \
    (flow_name == 'tt11') & \
    (dataset_case.str.startswith(\"3-\")) & \
    (((network == 'MA1' | network == 'MA2') & (type == 'SFA_DELAY_BOUND')) | \
    ((network == 'Baseline') & (type == 'TFA_DELAY_BOUND'))) \
)"
).replace(
    ['3-1-MA1', '3-1-MA2', '3-1-Baseline', '3-2-MA1', '3-2-MA2', '3-2-Baseline', '3-3-MA1', '3-3-MA2', '3-3-Baseline'], 
    ['1', '1', '1', '2', '2', '2', '3', '3', '3']
).sort_values(by=['network','dataset_case'], ascending=[True,True])


data4=analysesResult.query(
"(                          \
    (flow_name == 'tt11') & \
    (dataset_case.str.startswith(\"4-\")) & \
    (((network == 'MA1' | network == 'MA2') & (type == 'SFA_DELAY_BOUND')) | \
    ((network == 'Baseline') & (type == 'TFA_DELAY_BOUND'))) \
)"
).replace(
    ['4-1-MA1', '4-1-MA2', '4-1-Baseline', '4-2-MA1', '4-2-MA2', '4-2-Baseline', '4-3-MA1', '4-3-MA2', '4-3-Baseline'], 
    ['1', '1', '1', '2', '2', '2', '3', '3', '3']
).sort_values(by=['network','dataset_case'], ascending=[True,True])


experiment1 = sns.catplot(kind="bar", 
                x="dataset_case",
                y="worst_case_delay", 
                hue="network",
                data=data1,
                saturation=.5, 
                ci=None, 
                dodge=True, 
                legend=True,
                palette=choosen_palette,
                height=3, 
                aspect=2,
                )

experiment2 = sns.catplot(kind="bar", 
                x="dataset_case",
                y="worst_case_delay", 
                hue="network",
                data=data2,
                saturation=.5, 
                ci=None, 
                dodge=True, 
                legend=True,
                palette=choosen_palette,
                height=3, 
                aspect=2
                )             

experiment3 = sns.catplot(kind="bar", 
                x="dataset_case",
                y="worst_case_delay", 
                hue="network",
                data=data3,
                saturation=.5, 
                ci=None, 
                dodge=True, 
                legend=True,
                palette=choosen_palette,
                height=3, 
                aspect=2
                ) 

experiment4 = sns.catplot(kind="bar", 
                x="dataset_case",
                y="worst_case_delay", 
                hue="network",
                data=data4,
                saturation=.5, 
                ci=None, 
                dodge=True, 
                legend=True,
                palette=choosen_palette,
                height=3, 
                aspect=2
                )              

# extract the matplotlib axes_subplot objects from the FacetGrid
ax1 = experiment1.facet_axis(0, 0)
ax2 = experiment2.facet_axis(0, 0)
ax3 = experiment3.facet_axis(0, 0)
ax4 = experiment4.facet_axis(0, 0)


choosen_rotation = 90
#choosen_padding = 2
choosen_padding = 2
choosen_label_type = 'edge'

# iterate through the axes containers
for c1 in ax1.containers:
    labels1 = [f'{(v.get_height()):.1f}' for v in c1]
    ax1.bar_label(c1, labels=labels1, label_type=choosen_label_type, rotation=choosen_rotation, padding=choosen_padding)


# iterate through the axes containers
for c2 in ax2.containers:
    labels2 = [f'{(v.get_height()):.1f}' for v in c2]
    ax2.bar_label(c2, labels=labels2, label_type=choosen_label_type, rotation=choosen_rotation, padding=choosen_padding)

# iterate through the axes containers
for c3 in ax3.containers:
    labels3 = [f'{(v.get_height()):.1f}' for v in c3]
    ax3.bar_label(c3, labels=labels3, label_type=choosen_label_type, rotation=choosen_rotation, padding=choosen_padding)

# iterate through the axes containers
for c4 in ax4.containers:
    labels4 = [f'{(v.get_height()):.1f}' for v in c4]
    ax4.bar_label(c4, labels=labels4, label_type=choosen_label_type, rotation=choosen_rotation, padding=choosen_padding)


experiment1.set_axis_labels("Experiment 1 - Different Overlapping Scenarios", "Worst Case Delay ($\mu s$) - Flow tt11")
experiment1.set_xticklabels(["Case 1", "Case 2", "Case 3", "Case 4"])
experiment1.set_titles("{col_name}")
experiment1.tight_layout()

experiment2.set_axis_labels("Experiment 2 - Different Lengths of Open Windows", "Worst Case Delay ($\mu s$) - Flow tt11")
experiment2.set_xticklabels(["Case 1", "Case 2", "Case 3"])
experiment2.set_titles("{col_name}")
experiment2.tight_layout()

experiment3.set_axis_labels("Experiment 3 - Different Open-Close Cycles", "Worst Case Delay ($\mu s$) - Flow tt11")
experiment3.set_xticklabels(["Case 1", "Case 2", "Case 3"])
experiment3.set_titles("{col_name}")
experiment3.tight_layout()

experiment4.set_axis_labels("Experiment 4 - Different Priority Assigned", "Worst Case Delay ($\mu s$) - Flow tt11")
experiment4.set_xticklabels(["Case 1", "Case 2", "Case 3"])
experiment4.set_titles("{col_name}")
experiment4.tight_layout()

plt.show()