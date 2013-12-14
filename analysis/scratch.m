%Histogram of # tests run per problem solved

data1 = csv2struct('NaiveBaseline_TerrainMag4_ExpLog.csv');
data2 = csv2struct('NaiveBaseline_TerrainMag4_EarlyExit1_ExpLog.csv');
data3 = csv2struct('NaiveBaseline_TerrainMag4_EarlyExit2_ExpLog.csv');
data4 = csv2struct('NaiveBaseline_TerrainMag4_EarlyExit2_SCO_ExpLog.csv');


for data = [data1, data2]
    figure;
    [~, uniqueSolveLogIndices, ~] = unique(data.Num_Solved);
    plot(data.Num_Solved(uniqueSolveLogIndices), [data.Num_Tests(uniqueSolveLogIndices), data.Num_Challenges(uniqueSolveLogIndices)]);
    axis([0, 200, 0 , 1000]);
    % hist(diff(NumTests(uniqueSolveLogIndices)))
end