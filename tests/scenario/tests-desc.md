Test 1 : 

use a provided csrf.html page (that probably include an already forged link to an action)
--> Not possible to automate ; it is not possible to check some logs from a cypress test

Workaround : We may test that the action is call correctly without checking the server log!! Maybe changing action code to return something

Test 2 : C2455167 Verify Update TextAction

Login to the application
Go to home page,
Check that CSRF is loaded and has populated the form with the correct values
Check that once submitted, the form has update the page with the correct values
Check that the action has been performed

--> Implemented in cypress

Test 3 : C2455168 Remove an action from the whitelist

--> Not possible to automate ; maybe it is possible to apply a provisioning script inside the test to update configuration ?
Try to use provisonning api to update the configuration but there is an error in the config probably, disabled for now.

Test 4 : 

--> Hard to implement as it requires many pretest steps 

Test 5 : 

--> Hard to implement as it requires many pretest steps

Test 6 : 

--> Hard to implement as it requires many pretest steps including specific host configuration

Test 7 : 

--> Should be possible to execute but requires to update tests to a clustered configuration
