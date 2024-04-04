import logo from './logo.svg';
import './App.css';
import {Button, Switch} from "antd";

function App() {
  return (
    <div className="App">
        <Button type="primary">Hallo 2</Button>
        <br/>
        <Switch defaultChecked={true}/>
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>





    </div>
  );
}

export default App;
