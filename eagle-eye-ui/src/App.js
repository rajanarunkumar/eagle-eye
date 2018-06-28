import React, { Component } from 'react';
import './App.css';
import Header from "./components/Header";
import Webcam from "./components/Webcam";

class App extends Component {
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
