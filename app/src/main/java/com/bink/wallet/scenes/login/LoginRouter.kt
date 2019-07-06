package com.bink.wallet.scenes.login

interface LoginRoutingLogic
{
    //fun routeToSomewhere(segue: UIStoryboardSegue?)
}

interface LoginDataPassing
{
    var dataStore: LoginDataStore?
}

interface ILoginRouter : LoginRoutingLogic, LoginDataPassing {
}


class LoginRouter : ILoginRouter {

    var activity: LoginActivity? = null
    override var dataStore: LoginDataStore? = null

    // MARK: Routing

    //func routeToSomewhere(segue: UIStoryboardSegue?)
    //{
    //  if let segue = segue {
    //    let destinationVC = segue.destination as! SomewhereViewController
    //    var destinationDS = destinationVC.router!.dataStore!
    //    passDataToSomewhere(source: dataStore!, destination: &destinationDS)
    //  } else {
    //    let storyboard = UIStoryboard(name: "Main", bundle: nil)
    //    let destinationVC = storyboard.instantiateViewController(withIdentifier: "SomewhereViewController") as! SomewhereViewController
    //    var destinationDS = destinationVC.router!.dataStore!
    //    passDataToSomewhere(source: dataStore!, destination: &destinationDS)
    //    navigateToSomewhere(source: viewController!, destination: destinationVC)
    //  }
    //}

    // MARK: Navigation

    //func navigateToSomewhere(source: LoginViewController, destination: SomewhereViewController)
    //{
    //  source.show(destination, sender: nil)
    //}

    // MARK: Passing data

    //func passDataToSomewhere(source: LoginDataStore, destination: inout SomewhereDataStore)
    //{
    //  destination.name = source.name
    //}
}