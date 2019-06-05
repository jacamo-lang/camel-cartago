!start.

+!start
  <-  .print("-----Agent started");
      createWorkspace("room");
      !!increase(10);
      .wait(10000);
      joinWorkspace("room", _);
      .print("Workspace room joined");
      !!increase(10);
      .wait(10000);
      focus(dummyArt);
      .print("Artifact dummyArt focused");
      !!increase(10);
      .wait(10000);
      !!increase(20);
      .wait(10000);
  .

+!baseCycle
   <- createWorkspace("room");
      createWorkspace("kitchen");
      joinWorkspace("room", _);
      focus(dummyArt);

      .wait(3000);
      .print("-----Sending 10");
      operate(10);

      .wait(3000);
      .print("-----Updating to 15");
      operate(15);

      .wait(3000);
      .print("-----Sending 20 on room");
      operateRoom(20);

      // .wait(3000);
      // .print("-----Sending 30 on kitchen");
      // operateKitchen(30);

      .wait(3000);
      .print("-----Signalizing artifact")
      signalize(ok)

      .wait(3000);
      .print("-----Signalizing on room")
      signalizeRoom(ok)

      .wait(3000);
      .print("-----Signalizing on kitchen")
      signalizeKitchen(ok)

      .print("-----Done");
      .

+!increase(Val)
  <-  updateProperty(Val);
      .print("Defining property value to ", Val);
  .

-!increase(_)
  <-  .print("Operation failed").

+tick
  <- .print("Received signal");
  .

+propertyFoo(Val)
  <-  .print("Observable property 'propertyFoo', now has value ", Val).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
