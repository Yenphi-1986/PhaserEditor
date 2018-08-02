// The MIT License (MIT)
//
// Copyright (c) 2015, 2018 Arian Fornaris
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit
// persons to whom the Software is furnished to do so, subject to the
// following conditions: The above copyright notice and this permission
// notice shall be included in all copies or substantial portions of the
// Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
// NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.
package phasereditor.assetpack.core.animations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class AnimationModel {

	private String _key;
	private List<AnimationFrameModel> _frames;
	private int _duration;
	private double _frameRate;
	private int _repeat;
	private int _delay;
	private int _repeatDelay;
	private boolean _yoyo;
	private boolean _showOnStart;
	private boolean _hideOnComplete;
	private boolean _skipMissedFrames;
	private int _totalDuration;

	public AnimationModel() {
		_frames = new ArrayList<>();
		_frameRate = 24;
		_duration = 0;
		_delay = 0;
		_repeatDelay = 0;
		_yoyo = false;
		_showOnStart = false;
		_hideOnComplete = false;
		_skipMissedFrames = true;
	}

	public AnimationModel(JSONObject jsonData) {
		this();

		_key = jsonData.getString("key");

		var defaultTextureKey = jsonData.optString("defaultTextureKey", null);

		// TODO: missing parse frames provided by a string literal (spritesheet).

		var jsonFrames = jsonData.getJSONArray("frames");

		for (int i = 0; i < jsonFrames.length(); i++) {
			var jsonFrame = jsonFrames.getJSONObject(i);
			if (!jsonFrame.has("key") && defaultTextureKey != null) {
				jsonFrame.put("key", defaultTextureKey);
			}

			var frame = createAnimationFrame(jsonFrame);
			_frames.add(frame);
		}

		if (!jsonData.has("duration") && !jsonData.has("frameRate")) {
			// No duration or frameRate given, use default frameRate of 24fps
			_frameRate = 24;
			if (!_frames.isEmpty()) {
				_duration = (int) (_frameRate / _frames.size() * 1000);
			}
		} else if (jsonData.has("duration") && !jsonData.has("frameRate")) {
			// Duration given but no frameRate, so set the frameRate based on duration
			// I.e. 12 frames in the animation, duration = 4000 ms
			// So frameRate is 12 / (4000 / 1000) = 3 fps
			this._duration = jsonData.getInt("duration");
			_frameRate = _frames.size() / ((double) this._duration / 1000);
		} else {
			_frameRate = jsonData.getInt("frameRate");
			// frameRate given, derive duration from it (even if duration also specified)
			// I.e. 15 frames in the animation, frameRate = 30 fps
			// So duration is 15 / 30 = 0.5 * 1000 (half a second, or 500ms)
			if (_frameRate > 0) {
				_duration = (int) ((_frames.size() / _frameRate) * 1000);
			}
		}

		_repeat = jsonData.optInt("repeat", 0);
		_delay = jsonData.optInt("delay", 0);
		_repeatDelay = jsonData.optInt("repeatDelay", 0);
		_yoyo = jsonData.optBoolean("yoyo", false);
		_showOnStart = jsonData.optBoolean("showOnStart", false);
		_hideOnComplete = jsonData.optBoolean("hideOnComplete", false);
		_skipMissedFrames = jsonData.optBoolean("skipMissedFrames", true);

		buildTiming();
	}

	public void buildTiming() {
		_totalDuration = _duration;

		for (var frame : _frames) {
			if (frame.getDuration() > 0) {
				_totalDuration += frame.getDuration();
			}
		}

		int size = _frames.size();

		if (size > 0) {

			double time = 0;

			double avgFrameTime = _duration / size;

			for (var frame : _frames) {

				frame.setComputedFraction(time / _totalDuration);

				double frameRealDuration = avgFrameTime + frame.getDuration();

				frame.setComputedDuration((int) frameRealDuration);

				time += frameRealDuration;
			}

		}

	}

	public int getComputedTotalDuration() {
		return _totalDuration;
	}

	@SuppressWarnings("static-method")
	protected AnimationFrameModel createAnimationFrame(JSONObject jsonData) {
		return new AnimationFrameModel(jsonData);
	}

	public String getKey() {
		return _key;
	}

	public void setKey(String key) {
		_key = key;
	}

	public List<AnimationFrameModel> getFrames() {
		return _frames;
	}

	public int getDuration() {
		return _duration;
	}

	public void setDuration(int duration) {
		_duration = duration;
		_frameRate = _frames.size() / ((double) _duration / 1000);
	}

	public double getFrameRate() {
		return _frameRate;
	}

	public void setFrameRate(double frameRate) {
		_frameRate = Math.max(frameRate, 0);
		_duration = (int) ((_frames.size() / _frameRate) * 1000);
	}

	public int getRepeat() {
		return _repeat;
	}

	public void setRepeat(int repeat) {
		_repeat = repeat;
	}

	public int getDelay() {
		return _delay;
	}

	public void setDelay(int delay) {
		_delay = delay;
	}

	public int getRepeatDelay() {
		return _repeatDelay;
	}

	public void setRepeatDelay(int repeatDelay) {
		_repeatDelay = repeatDelay;
	}

	public boolean isYoyo() {
		return _yoyo;
	}

	public void setYoyo(boolean yoyo) {
		_yoyo = yoyo;
	}

	public boolean isShowOnStart() {
		return _showOnStart;
	}

	public void setShowOnStart(boolean showOnStart) {
		_showOnStart = showOnStart;
	}

	public boolean isHideOnComplete() {
		return _hideOnComplete;
	}

	public void setHideOnComplete(boolean hideOnComplete) {
		_hideOnComplete = hideOnComplete;
	}

	public boolean isSkipMissedFrames() {
		return _skipMissedFrames;
	}

	public void setSkipMissedFrames(boolean skipMissedFrames) {
		_skipMissedFrames = skipMissedFrames;
	}
}