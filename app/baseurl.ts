import {Injectable, OnInit}     from 'angular2/core';

@Injectable()
export class BaseUrlProvider implements OnInit
{
  public baseUrl = 'http://localhost:8080/user/';

  ngOnInit():any
  {
    console.log("BaseUrlProvider Init");
  }
}
