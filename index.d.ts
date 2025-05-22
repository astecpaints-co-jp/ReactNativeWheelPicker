import React from 'react';
import { DatePickerIOSProps, StyleProp, ViewStyle } from 'react-native';

export interface IWheelPickerItem {
  label: string;
  disabled?: boolean;
}


interface IStyle {
	selectedItemTextColor?: string;
	disabledItemTextColor?: string;
	selectedItemTextSize?: number;
	selectedItemTextFontFamily?: string;
	itemTextColor?: string;
	itemTextSize?: number;
	itemTextFontFamily?: string;
	indicatorColor?: string;
	hideIndicator?: boolean;
	indicatorWidth?: number;
	backgroundColor?: string;
	style?: StyleProp<ViewStyle>;
}

export interface IPropsWheelPicker extends IStyle {
	data: IWheelPickerItem[];
	isCyclic?: boolean;
	initPosition?: number;
	selectedItem?: number;
	onItemSelected?: (res: number) => void;
}

export class WheelPicker extends React.Component<IPropsWheelPicker> {
}

export interface IPropsTimePicker extends IStyle {
	initDate?: string;
	hours?: number[];
	minutes?: string[];
	format24?: boolean;
	onTimeSelected?: (res: Date) => void;
}

export class TimePicker extends React.Component<IPropsTimePicker> {
	constructor(props: IPropsTimePicker);
}

export interface IPropsDatePicker extends DatePickerIOSProps {
	initDate?: Date;
	days?: string[];
	hours?: string[];
	minutes?: string[];
	format24?: boolean;
	startDate?: Date;
	daysCount?: number;
	hideDate?: boolean;
	hideHours?: boolean;
	hideMinutes?: boolean;
	hideAM?: boolean;
	onDateSelected?: (res: Date) => void;
}

export class DatePicker extends React.Component<IPropsDatePicker> {
	constructor(props: IPropsDatePicker);
}

