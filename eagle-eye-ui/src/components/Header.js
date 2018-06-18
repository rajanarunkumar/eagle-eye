import React, { Component } from 'react';
import logo from '../eye.svg';
import '../App.css';

class Header extends Component {
    render() {
        return (
            <div className="Header-Div">
                <header className="App-header">
                    <img src={logo} className="App-logo" alt="logo" />
                    <h1 className="App-title">Welcome to Eagle Eye</h1>
                </header>
            </div>
        );
    }
}

export default Header;
