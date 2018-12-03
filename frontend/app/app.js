
var app = angular.module('app',['LocalStorageModule','ngRoute','socialLogin']);
var host = "";

app.config(function($routeProvider, $locationProvider) {
    
    // remove o # da url
    $locationProvider.html5Mode(true);



    $routeProvider

    .when('/login', {
        templateUrl: '/app/views/Login.html',
        controller: 'LoginController'
    })	
      .when('/signup', {	
        templateUrl: '/app/views/SignUp.html',	
        controller: 'SignUpController',
        requireAuth: true,
        requireNotRegistered: true
    })	

    .when('/userdata', {
        templateUrl: '/app/views/UserData.html',
        controller: 'UserDataController',
        requireAuth: true,
        requireNotRegistered:true
      
    })

    .when('/questoes', {	
        templateUrl: '/app/views/UserData.html',	
        controller: 'UserDataController',
        requireAuth: true,
        requireRegistered: true
    })	

    .when('/addQuestao', {	
        templateUrl: '/app/views/AddQuestao.html',	
        controller: 'SignUpController',
        requireAuth: true,
        requireRegistered: true
    })	
      .otherwise({
        redirectTo: '/login'
      });

    });



  app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.config(function (localStorageServiceProvider) {
    localStorageServiceProvider
      .setStorageType('sessionStorage')
      .setNotify(true, true)
  });


