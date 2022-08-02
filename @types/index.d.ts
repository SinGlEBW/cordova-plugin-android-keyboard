interface sendData {
  isShow: boolean;
  height: number;
}

type nameEvent = 'keyboard';
type cbSuccess = (data: sendData) => void;

interface AndroidKeyboard {
  getHeight(cb: (height: number) => void): void;
  on(nameEvent: nameEvent, cbSuccess: cbSuccess): void;
  off(nameEvent: nameEvent): void;

  // hide(): void;
  // show(): void;

}

declare interface Window {
  cordova: {
    plugins: {
      AndroidKeyboard: AndroidKeyboard;
    }
  },
}
