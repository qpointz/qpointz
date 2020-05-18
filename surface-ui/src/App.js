import React from 'react';

function App() {
  return (
      <>
        {/*<!-- Navbar --> */}
        <nav className="main-header navbar navbar-expand navbar-white navbar-light">

          {/*<!-- Left navbar links --> */}
          <ul className="navbar-nav">
            <li className="nav-item">
              <a className="nav-link" data-widget="pushmenu" href="/#" role="button"><i className="fas fa-bars"></i></a>
            </li>
            <li className="nav-item d-none d-sm-inline-block">
              <a href="index3.html" className="nav-link">Home</a>
            </li>
            <li className="nav-item d-none d-sm-inline-block">
              <a href='/#' className="nav-link">Contact</a>
            </li>
          </ul>

          {/*<!-- SEARCH FORM --> */}
          <form className="form-inline ml-3">
            <div className="input-group input-group-sm">
              <input className="form-control form-control-navbar" type="search" placeholder="Search" aria-label="Search"></input>
              <div className="input-group-append">
                <button className="btn btn-navbar" type="submit">
                  <i className="fas fa-search"></i>
                </button>
              </div>
            </div>
          </form>

          {/*<!-- Right navbar links --> */}
          <ul className="navbar-nav ml-auto">
            {/*<!-- Messages Dropdown Menu --> */}
            <li className="nav-item dropdown">
              <a className="nav-link" data-toggle="dropdown" href="/#">
                <i className="far fa-comments"></i>
                <span className="badge badge-danger navbar-badge">3</span>
              </a>
              <div className="dropdown-menu dropdown-menu-lg dropdown-menu-right">
                <a href="/#" className="dropdown-item">
                  {/*<!-- Message Start --> */}
                  <div className="media">
                    <img src="dist/img/user1-128x128.jpg" alt="User Avatar" className="img-size-50 mr-3 img-circle"></img>
                    <div className="media-body">
                      <h3 className="dropdown-item-title">
                        Brad Diesel
                        <span className="float-right text-sm text-danger"><i className="fas fa-star"></i></span>
                      </h3>
                      <p className="text-sm">Call me whenever you can...</p>
                      <p className="text-sm text-muted"><i className="far fa-clock mr-1"></i> 4 Hours Ago</p>
                    </div>
                  </div>
                  {/*<!-- Message End --> */}
                </a>
                <div className="dropdown-divider"></div>
                <a href="/#" className="dropdown-item">
                  {/*<!-- Message Start --> */}
                  <div className="media">
                    <img src="dist/img/user8-128x128.jpg" alt="User Avatar" className="img-size-50 img-circle mr-3"></img>
                    <div className="media-body">
                      <h3 className="dropdown-item-title">
                        John Pierce
                        <span className="float-right text-sm text-muted"><i className="fas fa-star"></i></span>
                      </h3>
                      <p className="text-sm">I got your message bro</p>
                      <p className="text-sm text-muted"><i className="far fa-clock mr-1"></i> 4 Hours Ago</p>
                    </div>
                  </div>
                  {/*<!-- Message End --> */}
                </a>
                <div className="dropdown-divider"></div>
                <a href="/#" className="dropdown-item">
                  {/*<!-- Message Start --> */}
                  <div className="media">
                    <img src="dist/img/user3-128x128.jpg" alt="User Avatar" className="img-size-50 img-circle mr-3"></img>
                    <div className="media-body">
                      <h3 className="dropdown-item-title">
                        Nora Silvester
                        <span className="float-right text-sm text-warning"><i className="fas fa-star"></i></span>
                      </h3>
                      <p className="text-sm">The subject goes here</p>
                      <p className="text-sm text-muted"><i className="far fa-clock mr-1"></i> 4 Hours Ago</p>
                    </div>
                  </div>
                  {/*<!-- Message End --> */}
                </a>
                <div className="dropdown-divider"></div>
                <a href="/#" className="dropdown-item dropdown-footer">See All Messages</a>
              </div>
            </li>
            {/*<!-- Notifications Dropdown Menu --> */}
            <li className="nav-item dropdown">
              <a className="nav-link" data-toggle="dropdown" href="/#">
                <i className="far fa-bell"></i>
                <span className="badge badge-warning navbar-badge">15</span>
              </a>
              <div className="dropdown-menu dropdown-menu-lg dropdown-menu-right">
                <span className="dropdown-item dropdown-header">15 Notifications</span>
                <div className="dropdown-divider"></div>
                <a href="/#" className="dropdown-item">
                  <i className="fas fa-envelope mr-2"></i> 4 new messages
                  <span className="float-right text-muted text-sm">3 mins</span>
                </a>
                <div className="dropdown-divider"></div>
                <a href="/#" className="dropdown-item">
                  <i className="fas fa-users mr-2"></i> 8 friend requests
                  <span className="float-right text-muted text-sm">12 hours</span>
                </a>
                <div className="dropdown-divider"></div>
                <a href="/#" className="dropdown-item">
                  <i className="fas fa-file mr-2"></i> 3 new reports
                  <span className="float-right text-muted text-sm">2 days</span>
                </a>
                <div className="dropdown-divider"></div>
                <a href="/#" className="dropdown-item dropdown-footer">See All Notifications</a>
              </div>
            </li>
            <li className="nav-item">
              <a className="nav-link" data-widget="control-sidebar" data-slide="true" href="/#" role="button">
                <i className="fas fa-th-large"></i>
              </a>
            </li>
          </ul>

        </nav>
        {/*<!-- /.navbar --> */}

        {/*<!-- Main Sidebar Container --> */}
        <aside className="main-sidebar sidebar-dark-primary elevation-4">
          {/*<!-- Brand Logo --> */}
          <a href="index3.html" className="brand-link">
            <img src="dist/img/AdminLTELogo.png"
                 alt="AdminLTE Logo"
                 className="brand-image img-circle elevation-3"
                 style={{opacity: .8}}></img>
            <span className="brand-text font-weight-light">AdminLTE 3</span>
          </a>

          {/*<!-- Sidebar --> */}
          <div className="sidebar">
            {/*<!-- Sidebar user (optional) --> */}
            <div className="user-panel mt-3 pb-3 mb-3 d-flex">
              <div className="image">
                <img src="dist/img/user2-160x160.jpg" className="img-circle elevation-2" alt="User Avatar"></img>
              </div>
              <div className="info">
                <a href="/#" className="d-block">Alexander Pierce</a>
              </div>
            </div>

            {/*<!-- Sidebar Menu --> */}
            <nav className="mt-2">
              <ul className="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu" data-accordion="false">
                {/*<!-- Add icons to the links using the .nav-icon class
                   with font-awesome or any other icon font library --> */}
                <li className="nav-item has-treeview">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon fas fa-tachometer-alt"></i>
                    <p>
                      Dashboard
                      <i className="right fas fa-angle-left"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="index.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Dashboard v1</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="index2.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Dashboard v2</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="index3.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Dashboard v3</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-item">
                  <a href="../widgets.html" className="nav-link">
                    <i className="nav-icon fas fa-th"></i>
                    <p>
                      Widgets
                      <span className="right badge badge-danger">New</span>
                    </p>
                  </a>
                </li>
                <li className="nav-item has-treeview menu-open">
                  <a href="/#" className="nav-link active">
                    <i className="nav-icon fas fa-copy"></i>
                    <p>
                      Layout Options
                      <i className="fas fa-angle-left right"></i>
                      <span className="badge badge-info right">6</span>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="../layout/top-nav.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Top Navigation</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../layout/top-nav-sidebar.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Top Navigation + Sidebar</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../layout/boxed.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Boxed</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../layout/fixed-sidebar.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Fixed Sidebar</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../layout/fixed-topnav.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Fixed Navbar</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../layout/fixed-footer.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Fixed Footer</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../layout/collapsed-sidebar.html" className="nav-link active">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Collapsed Sidebar</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-item has-treeview">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon fas fa-chart-pie"></i>
                    <p>
                      Charts
                      <i className="right fas fa-angle-left"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="../charts/chartjs.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>ChartJS</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../charts/flot.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Flot</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../charts/inline.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Inline</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-item has-treeview">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon fas fa-tree"></i>
                    <p>
                      UI Elements
                      <i className="fas fa-angle-left right"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="../UI/general.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>General</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../UI/icons.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Icons</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../UI/buttons.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Buttons</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../UI/sliders.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Sliders</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../UI/modals.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Modals & Alerts</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../UI/navbar.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Navbar & Tabs</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../UI/timeline.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Timeline</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../UI/ribbons.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Ribbons</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-item has-treeview">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon fas fa-edit"></i>
                    <p>
                      Forms
                      <i className="fas fa-angle-left right"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="../forms/general.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>General Elements</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../forms/advanced.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Advanced Elements</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../forms/editors.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Editors</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../forms/validation.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Validation</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-item has-treeview">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon fas fa-table"></i>
                    <p>
                      Tables
                      <i className="fas fa-angle-left right"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="../tables/simple.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Simple Tables</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../tables/data.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>DataTables</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../tables/jsgrid.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>jsGrid</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-header">EXAMPLES</li>
                <li className="nav-item">
                  <a href="../calendar.html" className="nav-link">
                    <i className="nav-icon far fa-calendar-alt"></i>
                    <p>
                      Calendar
                      <span className="badge badge-info right">2</span>
                    </p>
                  </a>
                </li>
                <li className="nav-item">
                  <a href="../gallery.html" className="nav-link">
                    <i className="nav-icon far fa-image"></i>
                    <p>
                      Gallery
                    </p>
                  </a>
                </li>
                <li className="nav-item has-treeview">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon far fa-envelope"></i>
                    <p>
                      Mailbox
                      <i className="fas fa-angle-left right"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="../mailbox/mailbox.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Inbox</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../mailbox/compose.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Compose</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../mailbox/read-mail.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Read</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-item has-treeview">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon fas fa-book"></i>
                    <p>
                      Pages
                      <i className="fas fa-angle-left right"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="../examples/invoice.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Invoice</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/profile.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Profile</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/e-commerce.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>E-commerce</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/projects.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Projects</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/project-add.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Project Add</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/project-edit.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Project Edit</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/project-detail.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Project Detail</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/contacts.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Contacts</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-item has-treeview menu-open">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon far fa-plus-square"></i>
                    <p>
                      Extras
                      <i className="fas fa-angle-left right"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="../examples/login.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Login</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/register.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Register</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/forgot-password.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Forgot Password</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/recover-password.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Recover Password</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/lockscreen.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Lockscreen</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/legacy-user-menu.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Legacy User Menu</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/language-menu.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Language Menu</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/404.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Error 404</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/500.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Error 500</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/pace.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Pace</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="../examples/blank.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Blank Page</p>
                      </a>
                    </li>
                    <li className="nav-item">
                      <a href="starter.html" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Starter Page</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-header">MISCELLANEOUS</li>
                <li className="nav-item">
                  <a href="https://adminlte.io/docs/3.0" className="nav-link">
                    <i className="nav-icon fas fa-file"></i>
                    <p>Documentation</p>
                  </a>
                </li>
                <li className="nav-header">MULTI LEVEL EXAMPLE</li>
                <li className="nav-item">
                  <a href="/#" className="nav-link">
                    <i className="fas fa-circle nav-icon"></i>
                    <p>Level 1</p>
                  </a>
                </li>
                <li className="nav-item has-treeview">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon fas fa-circle"></i>
                    <p>
                      Level 1
                      <i className="right fas fa-angle-left"></i>
                    </p>
                  </a>
                  <ul className="nav nav-treeview">
                    <li className="nav-item">
                      <a href="/#" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Level 2</p>
                      </a>
                    </li>
                    <li className="nav-item has-treeview">
                      <a href="/#" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>
                          Level 2
                          <i className="right fas fa-angle-left"></i>
                        </p>
                      </a>
                      <ul className="nav nav-treeview">
                        <li className="nav-item">
                          <a href="/#" className="nav-link">
                            <i className="far fa-dot-circle nav-icon"></i>
                            <p>Level 3</p>
                          </a>
                        </li>
                        <li className="nav-item">
                          <a href="/#" className="nav-link">
                            <i className="far fa-dot-circle nav-icon"></i>
                            <p>Level 3</p>
                          </a>
                        </li>
                        <li className="nav-item">
                          <a href="/#" className="nav-link">
                            <i className="far fa-dot-circle nav-icon"></i>
                            <p>Level 3</p>
                          </a>
                        </li>
                      </ul>
                    </li>
                    <li className="nav-item">
                      <a href="/#" className="nav-link">
                        <i className="far fa-circle nav-icon"></i>
                        <p>Level 2</p>
                      </a>
                    </li>
                  </ul>
                </li>
                <li className="nav-item">
                  <a href="/#" className="nav-link">
                    <i className="fas fa-circle nav-icon"></i>
                    <p>Level 1</p>
                  </a>
                </li>
                <li className="nav-header">LABELS</li>
                <li className="nav-item">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon far fa-circle text-danger"></i>
                    <p className="text">Important</p>
                  </a>
                </li>
                <li className="nav-item">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon far fa-circle text-warning"></i>
                    <p>Warning</p>
                  </a>
                </li>
                <li className="nav-item">
                  <a href="/#" className="nav-link">
                    <i className="nav-icon far fa-circle text-info"></i>
                    <p>Informational</p>
                  </a>
                </li>
              </ul>
            </nav>
            {/*<!-- /.sidebar-menu --> */}
          </div>
          {/*<!-- /.sidebar --> */}
        </aside>

        {/*<!-- Content Wrapper. Contains page content --> */}
        <div className="content-wrapper">
          {/*<!-- Content Header (Page header) --> */}
          <section className="content-header">
            <div className="container-fluid">
              <div className="row mb-2">
                <div className="col-sm-6">
                  <h1>Collapsed Sidebar</h1>
                </div>
                <div className="col-sm-6">
                  <ol className="breadcrumb float-sm-right">
                    <li className="breadcrumb-item"><a href="/#">Home</a></li>
                    <li className="breadcrumb-item"><a href="/#">Layout</a></li>
                    <li className="breadcrumb-item active">Collapsed Sidebar</li>
                  </ol>
                </div>
              </div>
            </div>{/*<!-- /.container-fluid --> */}
          </section>

          {/*<!-- Main content --> */}
          <section className="content">
            <div className="container-fluid">
              <div className="row">
                <div className="col-12">
                  {/*<!-- Default box --> */}
                  <div className="card">
                    <div className="card-header">
                      <h3 className="card-title">Title</h3>

                      <div className="card-tools">
                        <button type="button" className="btn btn-tool" data-card-widget="collapse" data-toggle="tooltip" title="Collapse">
                          <i className="fas fa-minus"></i></button>
                        <button type="button" className="btn btn-tool" data-card-widget="remove" data-toggle="tooltip" title="Remove">
                          <i className="fas fa-times"></i></button>
                      </div>
                    </div>
                    <div className="card-body">
                      Start creating your amazing application!
                    </div>
                    {/*<!-- /.card-body --> */}
                    <div className="card-footer">
                      Footer
                    </div>
                    {/*<!-- /.card-footer--> */}
                  </div>
                  {/*<!-- /.card --> */}
                </div>
              </div>
            </div>
          </section>
          {/*<!-- /.content --> */}
        </div>
        {/*<!-- /.content-wrapper --> */}

        <footer className="main-footer">
          <div className="float-right d-none d-sm-block">
            <b>Version</b> 3.0.4
          </div>
          <strong>Copyright &copy; 2014-2019 <a href="http://adminlte.io">AdminLTE.io</a>.</strong> All rights
          reserved.
        </footer>

        {/*<!-- Control Sidebar --> */}
        <aside className="control-sidebar control-sidebar-dark">
          {/*<!-- Control sidebar content goes here --> */}
        </aside>
        {/*<!-- /.control-sidebar --> */}

      </>
  )
}

export default App;
