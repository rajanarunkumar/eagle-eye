import React, { Component } from 'react';
import './App.css';
import Header from "./components/Header";
import Webcam from "./components/Webcam";

class App extends Component {
    constructor (props) {
        super(props);
    }

  render() {
    return (
      <div className="App">
        <Header/>
          <Webcam />
      </div>
    );
  }
}

export default App;
