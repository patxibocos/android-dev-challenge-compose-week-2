/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

sealed class State(val seconds: Int) {
    class Stopped(seconds: Int = 0) : State(seconds)

    //    class Started(seconds: Int) : State(seconds)
    class Countdown(seconds: Int) : State(seconds)
}

class MainViewModel : ViewModel() {
    private val _state = MutableLiveData<State>(State.Stopped())
    val state: LiveData<State> = _state
    private var currentTimer: Job? = null

    fun startTimer() {
        val currentState = _state.value
        if (currentState !is State.Stopped) {
            return
        }
        val seconds = currentState.seconds
        if (seconds <= 0) {
            return
        }
//        _state.value = State.Started(seconds)
        _state.value = State.Countdown(seconds)
        this.currentTimer = viewModelScope.launch {
            timer(seconds).collect {
                _state.value = if (it == 0) {
                    State.Stopped(0)
                } else {
                    State.Countdown(it)
                }
            }
        }
    }

    fun stopTimer() {
        currentTimer?.cancel()
        _state.value = State.Stopped(0)
    }

    fun incrementSecond() {
        val currentState = _state.value
        if (currentState !is State.Stopped) {
            return
        }
        _state.value = State.Stopped(currentState.seconds + 1)
    }

    fun decrementSecond() {
        val currentState = _state.value
        if (currentState !is State.Stopped) {
            return
        }
        if (currentState.seconds <= 0) {
            return
        }
        _state.value = State.Stopped(currentState.seconds - 1)
    }
}

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                val state by viewModel.state.observeAsState(State.Stopped())
                MyApp(
                    state,
                    viewModel::startTimer,
                    viewModel::stopTimer,
                    viewModel::incrementSecond,
                    viewModel::decrementSecond,
                )
            }
        }
    }
}

// Start building your app here!
@Composable
fun MyApp(
    state: State,
    timerStarted: () -> Unit = {},
    timerStopped: () -> Unit = {},
    secondsIncremented: () -> Unit = {},
    secondsDecremented: () -> Unit = {},
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Timer(state, timerStarted, timerStopped, secondsIncremented, secondsDecremented)
    }
}

@Composable
fun Timer(
    state: State,
    timerStarted: () -> Unit = {},
    timerStopped: () -> Unit = {},
    secondsIncremented: () -> Unit = {},
    secondsDecremented: () -> Unit = {},
) {
    Box(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (state) {
                is State.Countdown -> {
                    Text(
                        text = "${state.seconds}",
                        style = MaterialTheme.typography.h1
                    )
                    Button(
                        onClick = { timerStopped() },
                        shape = CircleShape
                    ) {
                        Text("Stop")
                    }
                }
                is State.Stopped -> {
                    CircularProgressIndicator(progress = .3f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { secondsDecremented() },
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(15.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colors.secondary)
                                    .height(5.dp)
                            )
                        }
                        Text(
                            text = "${state.seconds}",
                            style = MaterialTheme.typography.h1,
                            modifier = Modifier.padding(start = 30.dp, end = 30.dp)
                        )
                        Button(
                            onClick = { secondsIncremented() },
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(15.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colors.secondary)
                                        .height(5.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colors.secondary)
                                        .width(5.dp)
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { timerStarted() },
                        shape = CircleShape,
                        enabled = state.seconds > 0
                    ) {
                        Text(text = "GO!")
                    }
                }
            }
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp(State.Stopped())
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp(State.Countdown(5))
    }
}

fun timer(seconds: Int): Flow<Int> = flow {
    for (s in (seconds - 1) downTo 0) {
        delay(1000L)
        emit(s)
    }
}
