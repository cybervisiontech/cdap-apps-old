<!DOCTYPE html>
<!--
  ~ Copyright © 2014 Cask Data, Inc.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy of
  ~ the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  ~ WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  ~ License for the specific language governing permissions and limitations under
  ~ the License.
  -->
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Sentiment Analysis Dashboard</title>

    <!-- Bootstrap core CSS -->
    <link rel="stylesheet" href="./css/bootstrap.css" type="text/css" >

    <!-- Add custom CSS here -->
    <link rel="stylesheet" href="./css/style.css" type="text/css">
      <link rel="stylesheet" href="./font-awesome/css/font-awesome.min.css">
  </head>

  <body>

    <div id="wrapper">
      <!-- Sidebar -->
      <nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
          <a class="navbar-brand" href="/senti">Sentiment Analysis</a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse navbar-ex1-collapse">

          <ul class="nav navbar-nav">
            <li class="dropdown messages-dropdown active">
              <a href="/senti">Dashboard</a>
            </li>
            <li class="dropdown messages-dropdown">
                <a href="http://localhost:9999/#/flows/sentiment:analysis/">CDAP</a>
            </li>
            <li class="dropdown messages-dropdown">
                <a href="http://www.cask.co">Cask</a>
            </li>
          </ul>
        </div><!-- /.navbar-collapse -->
      </nav>

      <div id="page-wrapper">

        <div class="row">
          <div class="col-lg-12">
            <h1>Dashboard <small>Sentiment Analysis Overview</small></h1>
              <br/>
            <p>Say something!</p>
              <form class="bs-example" id="text-inject-form">
                  <input type="text" class="form-control" placeholder="Type a phrase and click enter..." id="stream-inject-textarea">
                  <br />
                  <button style="float: right" class="btn btn-primary" type="submit" id="analyze-button">Analyze</button>
              </form>
              <br/>
              <br/><br/>

          </div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-3">
            <div class="panel panel-info">
              <div class="panel-heading positive">
                <div class="row">
                  <div class="col-xs-6">
                    <i class="icon-inbox icon-5x"></i>
                  </div>
                  <div class="col-xs-6 text-right">
                    <p class="announcement-heading" id="all-sentences-processed">0</p>
                    <p class="announcement-text">Processed!</p>
                  </div>
                </div>
              </div>
              <a href="#">
              </a>
            </div>
          </div>
          <div class="col-lg-3">
            <div class="panel panel-success">
              <div class="panel-heading neutral">
                <div class="row">
                  <div class="col-xs-6">
                    <i class="icon-thumbs-up icon-5x"></i>
                  </div>
                  <div class="col-xs-6 text-right">
                    <p class="announcement-heading" id="positive-sentences-processed">0</p>
                      <p class="announcement-text">Positive</p>
                  </div>
                </div>
              </div>
              <a href="#">
              </a>
            </div>
          </div>
          <div class="col-lg-3">
            <div class="panel panel-danger">
              <div class="panel-heading">
                <div class="row">
                  <div class="col-xs-6">
                    <i class="icon-thumbs-down icon-5x"></i>
                  </div>
                  <div class="col-xs-6 text-right">
                    <p class="announcement-heading" id="negative-sentences-processed">0</p>
                      <p class="announcement-text">Negative</p>
                  </div>
                </div>
              </div>
              <a href="#">
              </a>
            </div>
          </div>
          <div class="col-lg-3">
              <div class="panel panel-gray">
                  <div class="panel-heading panel negative" id="neutral-sentences-panel">
                      <div class="row">
                          <div class="col-xs-6">
                              <i class="icon-ok icon-5x"></i>
                          </div>
                          <div class="col-xs-6 text-right">
                              <p class="announcement-heading" id="neutral-sentences-processed">0</p>
                              <p class="announcement-text">Neutral</p>
                          </div>
                      </div>
                  </div>
                  <a href="#">
                  </a>
              </div>
          </div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-4">
            <div class="panel panel-primary">
              <div class="panel-heading" id="panel-positive">
                <h3 class="panel-title">Unique Positive Sentences</h3>
              </div>
              <div class="panel-body">
                <div class="table-responsive">
                  <a id="positive-sentences-table-link"></a>
                  <table class="table table-bordered table-hover table-striped tablesorter" id="positive-sentences-table">
                    <thead>
                      <tr>
                        <th>Phrase</th>
                      </tr>
                    </thead>
                    <tbody>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
          <div class="col-lg-4">
              <div class="panel panel-primary">
                  <div class="panel-heading" id="panel-negative">
                      <h3 class="panel-title">Unique Negative Sentences</h3>
                  </div>
                  <div class="panel-body">
                      <div class="table-responsive">
                          <a id="negative-sentences-table-link"></a>
                          <table class="table table-bordered table-hover table-striped tablesorter" id="negative-sentences-table">
                              <thead>
                              <tr>
                                  <th>Phrase</th>
                              </tr>
                              </thead>
                              <tbody>
                              </tbody>
                          </table>
                      </div>
                  </div>
              </div>
          </div>
          <div class="col-lg-4">
              <div class="panel panel-primary">
                  <div class="panel-heading" id="panel-neutral">
                      <h3 class="panel-title">Unique Neutral Sentences</h3>
                  </div>
                  <div class="panel-body">
                      <div class="table-responsive">
                          <a id="neutral-sentences-table-link"></a>
                          <table class="table table-bordered table-hover table-striped tablesorter" id="neutral-sentences-table">
                              <thead>
                              <tr>
                                  <th>Phrase</th>
                              </tr>
                              </thead>
                              <tbody>
                              </tbody>
                          </table>
                      </div>
                  </div>
              </div>
          </div>
        </div><!-- /.row -->

      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- Bootstrap core JavaScript -->
    <script src="third_party/jquery-1.9.1.js"></script>
    <script src="third_party/bootstrap.js"></script>
    <script src="third_party/tablesorter/jquery.tablesorter.js"></script>
    <script src="third_party/tablesorter/tables.js"></script>
    <script src="js/main.js"></script>
  </body>
</html>