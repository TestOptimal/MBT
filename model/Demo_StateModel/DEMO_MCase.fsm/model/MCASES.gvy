// Script
$EXEC.addMCase('MCase 1').navigateToState('V75Cents').executeTransition('add50', { state -> $SYS.log('MCase 1')});

$EXEC.addMCase('MCase 2').navigateToState('V50Cents').executeTransition('add25', { state -> $SYS.log('MCase 2')});

$EXEC.addMCase('MCase 3').navigateToState('V50Cents').executeTransition('add25', { state -> $SYS.log('MCase 3')}).skipToState('End');

$EXEC.addMCase('MCase 4').navigateToState('V50Cents').executeTransition('add25', { state -> $SYS.log('MCase 4')}).skipToState('V100Cents');

$EXEC.addMCase('MCase 5').navigateToState('V25Cents').executeTransition('add25', { state -> $SYS.log('MCase 5')}).skipToState('V75Cents').executeTransition('add25', {state -> $SYS.log('Mcase 5, exec trans add25 2nd')});
