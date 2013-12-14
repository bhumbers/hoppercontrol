%Histogram of # tests run per problem solved

data1 = csv2struct('BigRange_ExpLog.csv');
data2 = csv2struct('BigRange_SCO_ExpLog.csv');
% data2 = csv2struct('NaiveBaseline_TerrainMag4_EarlyExit1_ExpLog.csv');
% data3 = csv2struct('NaiveBaseline_TerrainMag4_EarlyExit2_ExpLog.csv');
% data4 = csv2struct('NaiveBaseline_TerrainMag4_EarlyExit2_SCO_ExpLog.csv');


figure;
for data = [data1, data2]
    [~, uniqueSolveLogIndices, ~] = unique(data.Num_Solved);
    plot(data.Num_Solved(uniqueSolveLogIndices), data.Num_Tests(uniqueSolveLogIndices));
    hold all;
%     xlim([-1, -1])
    ylim([0 , 3500]);
    
%     figure;
%     hist(diff(data.Num_Tests(uniqueSolveLogIndices)))
end