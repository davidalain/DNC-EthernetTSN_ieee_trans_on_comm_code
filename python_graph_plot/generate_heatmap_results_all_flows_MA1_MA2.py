from dataclasses import replace
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
import pandas as pd
from pathlib import Path

sns.set_theme(style="white")
sns.set(font_scale=1.182)

pd.options.mode.use_inf_as_na = True

current_path = Path(__file__).parent
analysesResult = pd.read_csv(current_path / "../results/AnalysesResultForPythonSeaborn.csv", sep=";")

# Replace values
analysesResult = analysesResult.replace(
    ['1000BASE-TX'  , '1000BASE-T1S', 'network', 'tt10' , 'tt11', 'tt12', 'tt13'], 
    ['MA1'          , 'MA2'         , 'Network', 'ttA'  , 'ttB' , 'ttC' , 'ttD' ]
)

# Experiment1
analysesResult_exp1 = analysesResult.query(
    "(                          \
        ( (network == 'MA1' | network == 'MA2') & (type == 'SFA_DELAY_BOUND') & (dataset_case.str.startswith('1-')) ) \
    )"
)

analysesResult_exp2 = analysesResult.query(
    "(                          \
        ( (network == 'MA1' | network == 'MA2') & (type == 'SFA_DELAY_BOUND') & (dataset_case.str.startswith('2-')) ) \
    )"
)

analysesResult_exp3 = analysesResult.query(
    "(                          \
        ( (network == 'MA1' | network == 'MA2') & (type == 'SFA_DELAY_BOUND') & (dataset_case.str.startswith('3-')) ) \
    )"
)

analysesResult_exp4 = analysesResult.query(
    "(                          \
        ( (network == 'MA1' | network == 'MA2') & (type == 'SFA_DELAY_BOUND') & (dataset_case.str.startswith('4-')) ) \
    )"
)

print(analysesResult_exp1)
print(analysesResult_exp2)
print(analysesResult_exp3)
print(analysesResult_exp4)

choosen_palette="bright"


# Load the example flights dataset and convert to long-form
experiment1 = analysesResult_exp1.pivot("flow_name", "dataset_case", "worst_case_delay")
experiment2 = analysesResult_exp2.pivot("flow_name", "dataset_case", "worst_case_delay")
experiment3 = analysesResult_exp3.pivot("flow_name", "dataset_case", "worst_case_delay")
experiment4 = analysesResult_exp4.pivot("flow_name", "dataset_case", "worst_case_delay")

print(experiment1)
print(experiment2)
print(experiment3)
print(experiment4)

#vmin=343.5, vmax=3842.1

# Draw a heatmap with the numeric values in each cell
f1, ax1 = plt.subplots(figsize=(9, 6))

mask = experiment1.isna()
ax1.set_facecolor("black")


sns.heatmap(data=experiment1, annot=True, linewidths=.5, ax=ax1, cmap="YlGnBu", fmt=".1f", vmin=343.5, vmax=3842.1, mask=mask)
ax1.set_title('Experiment 1 - Different Overlapping Scenarios')
ax1.set_xlabel('Scheduling Cases')
ax1.set_ylabel('Flow')
ax1.set_xticklabels(["MA1\nCase 1", "MA2\nCase 1", "MA1\nCase 2", "MA2\nCase 2",  "MA1\nCase 3", "MA2\nCase 3", "MA1\nCase 4", "MA2\nCase 4"])
ax1.set_yticklabels(["tt1", "tt2", "tt3", "tt4", "tt5", "tt6", "tt7", "tt8", "tt9", "tt10", "tt11", "tt12", "tt13"])
for i in ax1.get_yticklabels():
    i.set_ha('right')
    i.set_rotation(0)
for i in ax1.get_xticklabels():
    i.set_ha('center')
f1.tight_layout()

# Draw a heatmap with the numeric values in each cell
f2, ax2 = plt.subplots(figsize=(9, 6))
sns.heatmap(data=experiment2, annot=True, linewidths=.5, ax=ax2, cmap="YlGnBu", fmt=".1f")
ax2.set_title('Experiment 2 - Different Lengths of Open Windows')
ax2.set_xlabel('Scheduling Cases')
ax2.set_ylabel('Flow')
ax2.set_xticklabels(["MA1\nCase 1", "MA2\nCase 1", "MA1\nCase 2", "MA2\nCase 2",  "MA1\nCase 3", "MA2\nCase 3"])
ax2.set_yticklabels(["tt1", "tt2", "tt3", "tt4", "tt5", "tt6", "tt7", "tt8", "tt9", "tt10", "tt11", "tt12", "tt13"])
for i in ax2.get_yticklabels():
    i.set_ha('right')
    i.set_rotation(0)
for i in ax2.get_xticklabels():
    i.set_ha('center')
f2.tight_layout()

# Draw a heatmap with the numeric values in each cell
f3, ax3 = plt.subplots(figsize=(9, 6))
sns.heatmap(data=experiment3, annot=True, linewidths=.5, ax=ax3, cmap="YlGnBu", fmt=".1f")
ax3.set_title('Experiment 3 - Different Open-Close Cycles')
ax3.set_xlabel('Scheduling Cases')
ax3.set_ylabel('Flow')
ax3.set_xticklabels(["MA1\nCase 1", "MA2\nCase 1", "MA1\nCase 2", "MA2\nCase 2",  "MA1\nCase 3", "MA2\nCase 3"])
ax3.set_yticklabels(["tt1", "tt2", "tt3", "tt4", "tt5", "tt6", "tt7", "tt8", "tt9", "tt10", "tt11", "tt12", "tt13"])
for i in ax3.get_yticklabels():
    i.set_ha('right')
    i.set_rotation(0)
for i in ax3.get_xticklabels():
    i.set_ha('center')
f3.tight_layout()

# Draw a heatmap with the numeric values in each cell
f4, ax4 = plt.subplots(figsize=(9, 6))
sns.heatmap(data=experiment4, annot=True, linewidths=.5, ax=ax4, cmap="YlGnBu", fmt=".1f")
ax4.set_title('Experiment 4 - Different Priority Assigned')
ax4.set_xlabel('Scheduling Cases')
ax4.set_ylabel('Flow')
ax4.set_xticklabels(["MA1\nCase 1", "MA2\nCase 1", "MA1\nCase 2", "MA2\nCase 2",  "MA1\nCase 3", "MA2\nCase 3"])
ax4.set_yticklabels(["tt1", "tt2", "tt3", "tt4", "tt5", "tt6", "tt7", "tt8", "tt9", "tt10", "tt11", "tt12", "tt13"])
for i in ax4.get_yticklabels():
    i.set_ha('right')
    i.set_rotation(0)
for i in ax4.get_xticklabels():
    i.set_ha('center')
f4.tight_layout()



#	    Min	    Máx	
# MA1	514.0	3469.9
# MA2	343.5	3842.1


plt.show()