counter(0).

!start.

+!start
  <-  .print("-----Agent started");
      focus(dummyArtifact);
      !updateCycle;
  .

+!updateCycle
  :   counter(X)
  <-  .wait(5000)
      updateProp(X);
      NewX = X + 1;
      -+counter(NewX);
      !updateCycle;
  .

+tick
  <-  .print("I, Alice, perceived a tick").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
