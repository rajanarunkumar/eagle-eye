import React, { Component } from 'react';

class Webcam extends Component {
    constructor (props) {
        super(props);
        this.state ={
            recorder:''
        };
        this.gotLocalMediaStream = this.gotLocalMediaStream.bind(this);
        this.handleLocalMediaStreamError = this.handleLocalMediaStreamError.bind(this);
        this.takePhoto = this.takePhoto.bind(this);
    }

    gotLocalMediaStream(mediaStream) {
        document.querySelector('video').srcObject = mediaStream;
    }

    handleLocalMediaStreamError(error) {
        console.log('navigator.getUserMedia error: ', error);
    }

    componentDidMount(){
        this.setState({recorder:navigator.mediaDevices.getUserMedia({
            video: true,})
            .then(this.gotLocalMediaStream).catch(this.handleLocalMediaStreamError)});
    }

    takePhoto(){
        let video = document.querySelector('video');
        let photo = document.getElementById('photo');
        let photoContext = photo.getContext('2d');
        photoContext.drawImage(video, 0, 0, photo.width, photo.height);
    }

    render() {
        return (
            <div className="App">
                <video id="camera" autoPlay playsInline>Cam</video>
                <button onClick={this.takePhoto}>Capture Target</button>
                <button>Transmit Snapshot</button>
                <button>Transmit Video</button>
                <canvas id="photo">canvas</canvas>
            </div>
        );
    }
}

export default Webcam;