
***Question***

Answer this hypothetical question in the README file:

Testing the CalculateRootsService for good input is pretty easy - we pass in a number and we expect a broadcast intent with the roots.
Testing for big prime numbers can be frustrating - currently the service is hard-coded to run for 20 seconds before giving up, which would make the tests run for too long.

**What would you change in the code in order to let the service run for maximum 200ms in tests environments,
  but continue to run for 20sec max in the real app (production environment)?*

I would have to pass a flag along with the extras, put the code responsible for sending the broadcast (the intent with extra) to the service
inside of a function where the default parameter is non-test mode, but when test mode flag is passed, that will in turn be passed to the service,
which in turn will give 200ms instead of 20 seconds for each boradcast.