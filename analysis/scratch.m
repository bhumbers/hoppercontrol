%Histogram of # tests run per problem solved

% data1 = csv2struct('BigRange_ExpLog.csv');
% data2 = csv2struct('BigRange_CE_ExpLog.csv');
% data3 = csv2struct('BigRange_SCO_ExpLog.csv');
% data4 = csv2struct('BigRange_CE_SCO_ExpLog.csv');
% data5 = csv2struct('BigRange_FCE_SCO_ExpLog.csv');

test1 = csv2struct('BigRange_TEST_FCE_ExpLog.csv'); %NOTE: This uses just the same 20-control ensemble as SCO
test2 = csv2struct('BigRange_TEST_EE2_12345_ExpLog.csv');
test3 = csv2struct('BigRange_TEST_SCO_ExpLog.csv');
test4 = csv2struct('BigRange_TEST_EE2_12345_SCO_ExpLog.csv');
test5 = csv2struct('BigRange_TEST_FCE_SCO_ExpLog.csv');
test6 = csv2struct('BigRange_TEST_EE2_12345_FCE_SCO_ExpLog.csv');

optimal = struct('Num_Tests', (1:1400)', 'Num_Solved', (1:1400)', ...
                    'Num_Failed', zeros(1400,1), 'Num_Unsolved', zeros(1400,1), ...
                    'Num_Challenges', zeros(1400,1));

solRangeOffset = 1; %full set
% solRangeOffset = 801; %only hard problems (4, 5, and 6 terrain mags)

data_of_interest = [test1, test2,test3, test4, test5, test6, optimal];
if (exist('num_tests_by_solve')), clear num_tests_by_solve; end

figure;
i = 1;
for data = data_of_interest
    [~, uniqueSolveLogIndices, ~] = unique(data.Num_Solved);
    %TEST: Only look at later solution behavior (more difficult problems)
    uniqueSolveLogIndices = uniqueSolveLogIndices(solRangeOffset:end);
    
    numSolved = data.Num_Solved(uniqueSolveLogIndices);
    numSolved = numSolved - numSolved(1);
    numTests = data.Num_Tests(uniqueSolveLogIndices);
    numTests = numTests - numTests(1);
    plot(numSolved, numTests);
    hold all;
%     xlim([-1, -1])
    ylim([0 , 3500]);
    
    num_tests_by_solve(:, i) = numTests;
    i = i + 1;
end
%Draw "best case" line
% line([0, 1500], [0, 1400]);

headers = { 'Baseline; 20 Control Ensemble', ...
            'Baseline; 200 Control Ensemble', ...
            'NN Lookup; 20 Control Ensemble; Sparse', ...
            'NN Lookup; 20 Control Ensemble; Dense', ...
            'NN Lookup; 200 Control Ensemble; Sparse', ...
            'NN Lookup; 200 Control Ensemble; Dense', ...
            'Optimal', ...
          }
csvwrite_with_headers('num_tests_all_runs_data.csv', num_tests_by_solve, headers);


for data = data_of_interest 
    [~, uniqueSolveLogIndices, ~] = unique(data.Num_Solved);
    %TEST: Only look at later solution behavior (more difficult problems)
    uniqueSolveLogIndices = uniqueSolveLogIndices(solRangeOffset:end);
    
    numSolved = data.Num_Solved(uniqueSolveLogIndices);
    numSolved = numSolved - numSolved(1);
    numTests = data.Num_Tests(uniqueSolveLogIndices);
    numTests = numTests - numTests(1);
    testsPerSolved = diff(numTests);
    avgTestsPerSolved = mean(testsPerSolved)
%     stdTestsPerSolved = std(testsPerSolved)
    
%     figure;
%     hist(testsPerSolved, 0:50); 
%     ylim([0, 2000]); 
end