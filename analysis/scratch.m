%Histogram of # tests run per problem solved

% data1 = csv2struct('BigRange_ExpLog.csv');
% data2 = csv2struct('BigRange_CE_ExpLog.csv');
% data3 = csv2struct('BigRange_SCO_ExpLog.csv');
% data4 = csv2struct('BigRange_CE_SCO_ExpLog.csv');
% data5 = csv2struct('BigRange_FCE_SCO_ExpLog.csv');

test1 = csv2struct('BigRange_TEST_ExpLog.csv');
test2 = csv2struct('BigRange_TEST_SCO_ExpLog.csv');
test3 = csv2struct('BigRange_TEST_FCE_SCO_ExpLog.csv');

data_of_interest = [test1, test2, test3];

figure;
for data = data_of_interest
    [~, uniqueSolveLogIndices, ~] = unique(data.Num_Solved);
    plot(data.Num_Solved(uniqueSolveLogIndices), data.Num_Tests(uniqueSolveLogIndices));
    hold all;
%     xlim([-1, -1])
    ylim([0 , 3500]);
end

for data = data_of_interest 
    [~, uniqueSolveLogIndices, ~] = unique(data.Num_Solved);
    testsPerSolved = diff(data.Num_Tests(uniqueSolveLogIndices));
    avgTestsPerSolved = mean(testsPerSolved)
%     stdTestsPerSolved = std(testsPerSolved)
    
    figure;
    hist(testsPerSolved, 0:50); 
    ylim([0, 2000]); 
end